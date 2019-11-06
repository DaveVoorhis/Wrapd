package org.reldb.wrapd.security;

import java.util.HashSet;

// Distantly based on https://www.codeproject.com/Articles/59186/Password-Strength-Control
public class PasswordStrength {
	public static int getPasswordStrength(String pwd) {
		int nScore = 0;
		int iUpperCase = 0;
		int iLowerCase = 0;
		int iDigit = 0;
		int iSymbol = 0;
		int iRepeated = 1;
		HashSet<Character> htRepeated = new HashSet<Character>();
		int iMiddle = 0;
		int iMiddleEx = 1;
		int consecutiveMode = 0;
		int iConsecutiveUpper = 0;
		int iConsecutiveLower = 0;
		int iConsecutiveDigit = 0;
		String sAlphas = "abcdefghijklmnopqrstuvwxyz";
		String sNumerics = "01234567890";
		int nSeqAlpha = 0;
		int nSeqNumber = 0;

		// Scan password
		for (char ch: pwd.toCharArray()) {
			
			// Count digits
			if (Character.isDigit(ch)) {
				iDigit++;
				if (consecutiveMode == 3)
					iConsecutiveDigit++;
				consecutiveMode = 3;
			}

			// Count uppercase characters
			if (Character.isUpperCase(ch)) {
				iUpperCase++;
				if (consecutiveMode == 1)
					iConsecutiveUpper++;
				consecutiveMode = 1;
			}

			// Count lowercase characters
			if (Character.isLowerCase(ch)) {
				iLowerCase++;
				if (consecutiveMode == 2)
					iConsecutiveLower++;
				consecutiveMode = 2;
			}

			// Count symbols
			if (!Character.isAlphabetic(ch) && !Character.isDigit(ch) && !(ch == ' ')) {
				iSymbol++;
				consecutiveMode = 0;
			}

			// Count repeated letters or digits
			if (Character.isLetter(ch) || Character.isDigit(ch)) {
				if (htRepeated.contains(Character.toLowerCase(ch)))
					iRepeated++;
				else
					htRepeated.add(Character.toLowerCase(ch));
				if (iMiddleEx > 1)
					iMiddle = iMiddleEx - 1;
			}
			
			if (iUpperCase > 0 || iLowerCase > 0)
				if (Character.isDigit(ch) || (!Character.isAlphabetic(ch) && !Character.isDigit(ch) && !(ch == ' ')))
					iMiddleEx++;
		}

		// Check for sequential alpha String patterns (forward and reverse)
		for (int s = 0; s < 23; s++) {
			String sFwd = sAlphas.substring(s, s + 3);
			String sRev = strReverse(sFwd);
			if (pwd.toLowerCase().indexOf(sFwd) != -1 || pwd.toLowerCase().indexOf(sRev) != -1)
				nSeqAlpha++;
		}

		// Check for sequential numeric String patterns (forward and reverse)
		for (int s = 0; s < 8; s++) {
			String sFwd = sNumerics.substring(s, s + 3);
			String sRev = strReverse(sFwd);
			if (pwd.toLowerCase().indexOf(sFwd) != -1 || pwd.toLowerCase().indexOf(sRev) != -1)
				nSeqNumber++;
		}

		// Score += 4 * Password Length
		nScore = 4 * pwd.length();

		// if we have uppercase letters  Score +=(number of uppercase letters *2)
		if (iUpperCase > 0)
			nScore += ((pwd.length() - iUpperCase) * 2);

		// if we have lowercase letters Score +=(number of lowercase letters *2)
		if (iLowerCase > 0)
			nScore += ((pwd.length() - iLowerCase) * 2);

		// Score += (Number of digits *4)
		nScore += (iDigit * 4);

		// Score += (Number of Symbols * 6)
		nScore += (iSymbol * 6);

		// Score += (Number of digits or symbols in middle of password *2)
		nScore += (iMiddle * 2);

		// requirements
		int requirements = 0;
		if (pwd.length() >= 8)
			requirements++; // Min password length
		if (iUpperCase > 0)
			requirements++; // Uppercase letters
		if (iLowerCase > 0)
			requirements++; // Lowercase letters
		if (iDigit > 0)
			requirements++; // Digits
		if (iSymbol > 0)
			requirements++; // Symbols

		// If we have more than 3 requirements then
		if (requirements > 3)
			nScore += (requirements * 2);

		//
		// Deductions
		//

		// If only letters then score -= password length
		if (iDigit == 0 && iSymbol == 0)
			nScore -= pwd.length();

		// If only digits then score -= password length
		if (iDigit == pwd.length())
			nScore -= pwd.length();

		// If repeated letters used then score -= (iRepeated * (iRepeated - 1));
		if (iRepeated > 1)
			nScore -= (iRepeated * (iRepeated - 1));

		// If Consecutive uppercase letters then score -= (iConsecutiveUpper * 2);
		nScore -= (iConsecutiveUpper * 2);

		// If Consecutive lowercase letters then score -= (iConsecutiveUpper * 2);
		nScore -= (iConsecutiveLower * 2);

		// If Consecutive digits used then score -= (iConsecutiveDigits * 2);
		nScore -= (iConsecutiveDigit * 2);

		// If password contains sequence of letters then score -= (nSeqAlpha * 3)
		nScore -= (nSeqAlpha * 3);

		// If password contains sequence of digits then score -= (nSeqNumber * 3)
		nScore -= (nSeqNumber * 3);

		/* Determine complexity based on overall score */
		if (nScore > 100)
			nScore = 100;
		else if (nScore < 0)
			nScore = 0;
		
		return nScore;
	}

	public static String getInterpretation(int nScore) {
		if (nScore < 20)
			return "Very Weak";
		else if (nScore >= 20 && nScore < 40)
			return "Weak";
		else if (nScore >= 40 && nScore < 60)
			return "Good";
		else if (nScore >= 60 && nScore < 80)
			return "Strong";
		else
			return "Very Strong";
	}
	
	private static String strReverse(String str) {
		String newString = "";
		for (int s = 0; s < str.length(); s++)
			newString = str.charAt(s) + newString;
		return newString;
	}
}
