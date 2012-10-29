<?php
/*
 * Copyright (C) 2011 Stefan Lohr
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Function for detecting the prefered browser language.
 */
function getLangFromBrowser($allowedLanguages, $defaultLanguage, $langVariable = null, $strictMode = true) {

	// if no language variable is passed in, take the language from $_SERVER['HTTP_ACCEPT_LANGUAGE']
	if ($langVariable === null) $langVariable = $_SERVER['HTTP_ACCEPT_LANGUAGE'];
	
	// if still no language information are available, return the default language
	if (empty($langVariable)) return $defaultLanguage;

	// Split the language string into his components
	$browserLanguages = preg_split('/,\s*/', $langVariable);

	// Set the current language with the default language and the current quality with 0
	$currentLang = $defaultLanguage;
	$currentQuality = 0;

	// interprete all available browser language settings
	foreach ($browserLanguages as $browserLanguage) {
		
		// Get all information about this language setting
		$pregMatchPattern = '/^([a-z]{1,8}(?:-[a-z]{1,8})*)(?:;\s*q=(0(?:\.[0-9]{1,3})?|1(?:\.0{1,3})?))?$/i';
		$matchCount = preg_match($pregMatchPattern, $browserLanguage, $matches);

		// could the language setting be read? if not, continue
		if (!$matchCount) continue;

		// get the language code and split it in his parts
		$languageCode = explode('-', $matches[1]);

		// if a quality is available set the quality variable
		if (isset($matches[2])) $langQuality = (float)$matches[2];
		// if no language quality is set, set the quality to 1.0 
		else $langQuality = 1.0;

		// repeat until all language codes are checked
		while (count($languageCode)) {
			
			// check if the language code is an allowed language code
			if (in_array(strtolower(implode('-', $languageCode)), $allowedLanguages)) {
				
				// check if the language quality is better than the current
				if ($langQuality > $currentQuality) {
					
					// set this language and quality as current language and quality 
					$currentLang = strtolower(implode('-', $languageCode));
					$currentQuality = $langQuality;
					
					// leave the inner while loop
					break;
				}
			}
			
			// if strict mode is set, leave the outer while loop
			if ($strictMode) break;
			
			// cut the right (checked) part of the arrays
			array_pop($languageCode);
		}
	}

	// return the best language
	return $currentLang;
}

?>
