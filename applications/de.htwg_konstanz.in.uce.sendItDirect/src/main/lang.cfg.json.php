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
 * Load the language config file with the default variables 
 */
require_once('lang.cfg.inc.php');

/**
 * Get the language identification code by GET or POST parameters
 * 
 * @var String with the language identification code
 */
$language = $_REQUEST['lang'];

/**
 * Check if the language is a allowed language, if not, set it to the default language
 */
if (!in_array($language, $allowedLanguages)) $language = $defaultLanguage;

/**
 * Get the file name of the needed language file
 * 
 * @var String with the name of the needed language file
 */
$languageFile = sprintf('lang.cfg.%s.json', $language);

/**
 * Send the content of the language file to browser
 */
readfile($languageFile);

?>
