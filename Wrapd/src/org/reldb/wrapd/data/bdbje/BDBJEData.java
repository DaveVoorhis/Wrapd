package org.reldb.wrapd.data.bdbje;

import static org.reldb.wrapd.strings.Strings.*;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.reldb.wrapd.data.Data;
import org.reldb.wrapd.exceptions.ExceptionFatal;
import org.reldb.wrapd.strings.Str;
import org.reldb.wrapd.tuples.Tuple;
import org.reldb.wrapd.tuples.TupleTypeGenerator;

public class BDBJEData<K extends Serializable, V extends Tuple> implements Data<K, V> {
	private BDBJEBase base;
	private String name;
	
	BDBJEData(BDBJEBase bdbjeBase, String name) {
		this.base = bdbjeBase;
		this.name = name;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Class<V> getType() {
		try {
			return (Class<V>)base.getTupleTypeOf(name);
		} catch (ClassNotFoundException e) {
			throw new ExceptionFatal(Str.ing(ErrUnableToLoadTupleTypeClass2, name));
		}
	}
	
	@SuppressWarnings("unchecked")
	private Class<? extends Tuple> copyOldToNew(Class<? extends Tuple> oldTupleClass, String newName) {
		Class<? extends Tuple> newTupleClass;
		try {
			newTupleClass = base.loadClass(newName);
		} catch (ClassNotFoundException e) {
			throw new ExceptionFatal(Str.ing(ErrUnableToLoadTupleTypeClass, newName));			
		}
		Method copyFrom;
		try {
			copyFrom = newTupleClass.getMethod("copyFrom", oldTupleClass);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new ExceptionFatal(Str.ing(ErrUnableToLocateCopyFromMethod, newName));
		}
		try {
			var newTemporaryName = newName;
			var newStorage = base.create(newTemporaryName);
			var newInstance = newTupleClass.getConstructor().newInstance();
			base.transaction(() -> {
				newStorage.access(newdata -> access(data -> data.forEach((key, value) -> {
					try {
						copyFrom.invoke(newInstance, value);
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						throw new ExceptionFatal(Str.ing(ErrSchemaUpdateCopyFromFailure, e.getMessage()));						
					}
					newdata.put(key, (V)newInstance);
				})));
				base.remove(name);
				newStorage.renameDataTo(name);
			});
		} catch (Exception e) {
			throw new ExceptionFatal(Str.ing(ErrSchemaUpdateFailure, e.getMessage()));
		}
		return newTupleClass;
	}
	
	@FunctionalInterface
	private static interface Action {
		public abstract void change(TupleTypeGenerator tupleTypeGenerator);
	}
	
	@FunctionalInterface
	private static interface Renamer {
		public abstract String newName(TupleTypeGenerator tupleTypeGenerator);
	}
	
	private void changeSchema(Action tupleTypeAction, Renamer tupleTypeRenamer) {
		String dbName = name;
		String oldTupleClassName;
		Class<? extends Tuple> oldTupleClass;
		try {
			oldTupleClassName = base.getTupleTypeNameOf(dbName);
			oldTupleClass = base.loadClass(oldTupleClassName);
		} catch (ClassNotFoundException e) {
			throw new ExceptionFatal(Str.ing(ErrUnableToLoadTupleType, dbName));
		}
		var codeDir = base.getCodeDir();
		var oldTupleTypeGenerator = new TupleTypeGenerator(codeDir, oldTupleClassName);
		var newName = tupleTypeRenamer.newName(oldTupleTypeGenerator);
		var tupleTypeGenerator = oldTupleTypeGenerator.copyTo(newName);
		if (tupleTypeAction != null)
			tupleTypeAction.change(tupleTypeGenerator);
		var compileResult = tupleTypeGenerator.compile();
		if (!compileResult.compiled)
			throw new ExceptionFatal(Str.ing(ErrUnableToExtendTupleType, newName, compileResult));
		var newTupleType = copyOldToNew(oldTupleClass, newName);
		base.updateCatalog(name, newTupleType);
		oldTupleTypeGenerator.destroy();		
	}

	private void changeSchema(Renamer renamer) {
		changeSchema(null, renamer);
	}
	
	private void changeSchema(Action tupleTypeAction) {
		changeSchema(tupleTypeAction, tupleTypeGenerator -> name + "_" + (tupleTypeGenerator.getSerial() + 1));
	}

	/** Rename this data store; do not rename associated tuple type. */
	public void renameDataTo(String newName) {
		base.rename(name, newName);
		name = newName;
	}

	/** Rename this data store and associated tuple type. */
	public void renameAllTo(String newName) {
		changeSchema((Renamer)tupleTypeGenerator -> newName);
		renameDataTo(newName);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String newName) {
		name = newName;
	}
	
	@Override
	public boolean isExtendable() {
		return true;
	}
	
	@Override
	public void extend(String name, Class<? extends Serializable> type) {
		changeSchema((Action)tupleTypeGenerator -> tupleTypeGenerator.addAttribute(name, type));
	}

	@Override
	public boolean isRemovable() {
		return true;
	}

	@Override
	public void remove(String name) {
		changeSchema((Action)tupleTypeGenerator -> tupleTypeGenerator.removeAttribute(name));
	}

	@Override
	public boolean isRenameable() {
		return true;
	}

	@Override
	public void rename(String oldName, String newName) {
		changeSchema((Action)tupleTypeGenerator -> tupleTypeGenerator.renameAttribute(oldName, newName));
	}

	@Override
	public boolean isTypeChangeable() {
		return true;
	}

	@Override
	public void changeType(String name, Class<? extends Serializable> type) {
		changeSchema((Action)tupleTypeGenerator -> tupleTypeGenerator.changeAttributeType(name, type));
	}
	
	@Override
	public boolean isReadonly() {
		return false;
	}

	@Override
	public <T> T query(Query<T> query) {
		return (T)base.query(this, query);
	}
	
	@Override
	public void access(Access access) {
		query(action -> {
			access.go(action);
			return null;
		});
	}
	
}
