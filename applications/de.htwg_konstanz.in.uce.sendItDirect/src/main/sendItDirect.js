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
 * Sets all default and global variables
 */
// later this variable contains the parameters for the JavaApplet
var param = null;

// the minimum version of Java which is needed
var vers = "1.6";

// the default IP of the MediatorServer
var mediatorIP = "141.37.121.124";

// the default Port of the MediatorServer
var mediatorPort = "10111";

// the URL base of this application for providing a file
var serviceURLBase = 'http://ice.in.htwg-konstanz.de/sid?fid=';

// attributes which are needed for the initialization of the JavaApplet
var attr = {
	
	// old option for JavaScript access from JavaApplet
	mayscript: "mayscript",
	
	// default height and width of the JavaApplet (invisible)  
	width: 0, height: 0,
	
	// URL to the binary source oh the JavaApplet
	archive: "./sendItDirect-0.1-SNAPSHOT_signed.jar",
	
	// name of the JavaApplet in DOM tree for calling methods
	name: "sendItDirectApplet",
	
	// name and packet of the initial called class of the JavaApplet
	code: "de.htwg_konstanz.in.uce.sendItDirect.SendItDirectApplet"
};

/**
 * If the complete DOM tree is loaded and the document is ready, the first function can be call.
 * This first function is a function called initializeJavaScriptPart, which will do all initial work.
 */
$(document).ready(initializeJavaScriptPart);

/**
 * If the window size of the browser changes, the dialog position must be refreshed.
 * On resize the function refreshDialogPosition will be called, which refreshes the position of all dialogs.
 */
$(window).resize(refreshDialogPosition);

/**
 * First JavaScript function which is called on page load. This function will do all the initial work.
 * This function will set the default dialog options, get the default language data and read the parameters.
 */
function initializeJavaScriptPart() {
	
	// set the default values for the dialogs
	$.extend($.ui.dialog.prototype.options, {
		
		// make a reposition of the dialog if the browser window size changes
		autoReposition: true,
		
		// prevent the closing of the dialog if ESC is typed
		closeOnEscape: false,
		
		// function which has to be called before a new dialog will be created
		create: newDialogCreated,
		
		// function which has to be called after a new dialog was created
		open: newDialogOpend,
		
		// function which has to be called after a dialog was closed
		close: dialogClosed,
		
		// default width of a new dialog window
		width: 480
	});
	
	// get the selected language identification code
	var language = $('#lang').attr('name');
	
	// sets the language file URL and the parameter with the selected language identification code
	var jsonUrl = 'lang.cfg.json.php';
	var jsonData = {'lang': language};
	
	// starts an AJAX request to get the language file data with the selected language identification code
	$.getJSON(jsonUrl, jsonData, loadLanguageAndStart);
	
	// sets the mouse cursor over the flag icons to an pointer
	$('#lang').find('img').css('cursor', 'pointer');
	
	// adds the click event to the image flags (on click call otherLanguageClicked function)
	$('#lang').find('img').click(otherLanguageClicked);
	
	// gets the default parameters or the parameters vom URL 
	param = getParameters();
}

/**
 * Function to prevent the site unload and shows an information before the page closes.
 * This is necessary, because the file won't be provided if the page will be closed.
 * If the user unloads the page, a message will be shown and the user can stop or continue the unload.
 * 
 * @param flag Boolean true or false (to prevent or not prevent the unload of the page)
 * @param message String message which will be shown on page unload
 */
function preventUnload(flag, message) {
	
	// tests, if the unload should be prevented or not
	if (flag) {
		
		// sets the function which will be called if the page closes
		window.onbeforeunload = function() {
			
			// returns the message which will be shown on page unload
			return $('#lang').data(message);
		};
	}
	// unset the page unload event 
	else window.onbeforeunload = null;
}

/**
 * Dependent on the selected language, the appropriate language file will be loaded.
 * The loaded language data will be passed into the loadLanguageAndRefresh function.
 */
function otherLanguageClicked() {
	
	// get the selected language identification code
	var language = $(this).attr('name');
	
	// sets the language file URL and the parameter with the selected language identification code
	var jsonUrl = 'lang.cfg.json.php';
	var jsonData = {'lang': language};
	
	// saves the selected language identification code
	$('#lang').attr('name', language);
	
	// starts an AJAX request to get the language file data with the selected language identification code
	$.getJSON(jsonUrl, jsonData, loadLanguageAndRefresh);
}

/**
 * This function loads the new language data into the memory.
 * 
 * @param data JSON language file data
 */
function loadLanguageData(data) {
	
	// save/get the selected language identification code
	var language = $('#lang').attr('name');
	
	// remove the old language data
	$('#lang').removeData();
	
	// set the saved selected language identification code
	$('#lang').attr('name', language);
	
	// set the complete language data from language file
	$('#lang').data(data);
	
	// change the 'change language' text in the browser
	$('#changeLanguageText').html($('#lang').data('changeLanguageText'));
}

/**
 * This function loads the new language data into the memory and refreshes the dialog windows.
 * 
 * @param data JSON language file data
 */
function loadLanguageAndRefresh(data) {
	
	// loads the language data from language file into memory
	loadLanguageData(data);
	
	// select each dialog for changing language
	$('.ui-dialog-content').each(function() {
		
		// get old dialog element and new dialog options
		var dialog = $(this).data('dialog');
		var dialogOptions = dialog.options;
		
		// close the old dialog window
		dialog.close();
		
		// open a new dialog window with the new settings and language
		$('<div/>').dialog(dialogOptions);
	});
}

/**
 * This function loads the language data into the memory and displays the initial dialog.
 * Dependent on the JavaApplet mode, it shows the server or the client start dialog. 
 * 
 * @param data JSON language file data
 */
function loadLanguageAndStart(data) {
	
	// loads the language data from language file into memory
	loadLanguageData(data);
	
	// displays the start dialog, dependent on the JavaApplet mode
	goBackToStartDialog();
}

/**
 * Function for showing 'start' dialog, depending on the mode (client or server) of the JavaApplet.
 * This function will also be called by clicking the back button in the 'information' dialog.
 * In this function will be decided if the server or the client 'start' dialog must be shown.
 */
function goBackToStartDialog() {
	
	// if no fileId is set, the JavaApplet works as server and the 'sender start message' will be shown
	if (param.fid == '') showSenderStartMessage();
	// else a fileId is set, the JavaApplet works as client and the 'receiver start message' will be shown
	else showReceiverStartMessage();
}

/**
 * Places all dialog windows in the center of the browser window.
 * This function will be called by the browser, if the window size was changed.
 */
function refreshDialogPosition() {
	
	// selects each dialog element for setting it to center position
	$('.ui-dialog-content:visible').each(function() {
		
		// get the dialog element itself
		var dialog = $(this).data('dialog');
		
		// if the autoReposition flag is set, set the dialog on his old position 
		if (dialog.options.autoReposition) {
			
			// place the dialog on his old/new position.
			// normally its the center of the window.
			dialog.option('position', dialog.options.position);
		}
	});
}

/**
 * Function which is called by jQuery (UI) after a new dialog was created.
 * This function calls, if it exists, the 'action' function of the dialog.
 * Additionally it fixes the height bug of the Internet Explorer.
 * 
 * @param event Event fired by jQuery (UI) dialog
 * @param ui jQuery (UI) element of the dialog
 */
function newDialogOpend(event, ui)  {
	
	// reads the 'action' information from the language file
	var dialog = $(this).data('dialog');
	var config = dialog.options.config;
	var action = $('#lang').data(config).action;
	
	// executes the 'action' information from the language file
	if (action) eval(action + '()');
	
	// fixes the height bug of the dialog in the Internet Explorer
	$(this).height('auto');
}

/**
 * Function which is called by jQuery (UI) before a new dialog will be created.
 * This function sets the content and buttons of the dialog.
 * The text of all will be read out of the language file date.
 * Also the close button of the dialog will be hidden.
 * 
 * @param event Event fired by jQuery (UI) dialog
 * @param ui jQuery (UI) element of the dialog
 */
function newDialogCreated(event, ui)  {

	// disable the close button of the dialog window
	$(".ui-dialog-titlebar-close", ui.dialog).hide();
	
	// load title, content and button informations form the language file
	var dialog = $(this).data('dialog');
	var config = dialog.options.config;
	var option = dialog.options;
	var buttons = $('#lang').data(config).buttons;
	
	// set the content of the dialog with the text from the language file
	$(".ui-dialog-content", ui.dialog).html($('#lang').data(config).content);
	
	// set the title of the dialog with the text from the language file
	dialog.option('title', $('#lang').data(config).title);
	
	// generate the button of the dialog with the information of the language file
	for (var text in buttons) buttons[text] = eval(buttons[text]);
	
	// set the generated buttons in the dialog file
	dialog.option('buttons', buttons);
	
	// center the dialog box on the window of the browser
	dialog.option('position', dialog.options.position);
}

/**
 * Function which is called by jQuery (UI) after a dialog is closed.
 * Clears die Memory and DOM from this dialog.
 * 
 * @param event Event fired by jQuery (UI) dialog
 * @param ui jQuery (UI) element of the dialog
 */
function dialogClosed(event, ui) {
	
	// deletes the text content of die dialog
	$(".ui-dialog-content", ui.dialog).html('');
	
	// destroys all other dialog elements
	$(this).data('dialog').destroy();
	
	// removes the clean dialog element from DOM tree
	$(this).remove();
}

/**
 * Displays the 'sender start message' dialog direct after page load.
 * This Message will be shown if the page is in server mode (send file)
 */
function showSenderStartMessage() {
	
	// close the old dialog fields
	$('.ui-dialog-content').dialog('close');
	
	var dialogOptions = {
		
		config: 'senderStartMessageDialog'
	};
	
	// displays the 'sender start message' dialog
	$('<div/>').dialog(dialogOptions);
}

/**
 * Displays the 'receiver start message' dialog direct after page load.
 * This Message will be shown if the page is in client mode (receive file)
 */
function showReceiverStartMessage() {
	
	// close the old dialog fields
	$('.ui-dialog-content').dialog('close');
	
	var dialogOptions = {
		
		config: 'receiverStartMessageDialog'
	};
	
	// displays the 'receiver start messages' dialog
	$('<div/>').dialog(dialogOptions);
}

/**
 * This function loads the JavaApplet and starts it.
 * This function is called by some button actions of the language file
 */
function loadApplet() {
	
	// close the old dialog fields
	$('.ui-dialog-content').dialog('close');
	
	var dialogOptions = {
		
		config: 'startingAppletDialog'
	};
	
	// displays the 'starting applet' dialog
	$('<div/>').dialog(dialogOptions);

	// uncached load by timestamp seems to do not work with jre7
	/*
	var zeit = new Date();
	var ms = zeit.getMilliseconds();
	attr.archive = attr.archive + "?" + ms;
	*/
	
	// sets the callback function in the JavaApplet parameters
	// this function will be called after the JavaApplet has been load
	param.callbackFunction = "sentItDirectAppletReady";
	
	// sets the message function in the JavaApplet parameters
	// this function will be called if the JavaApplet has a message or an error
	param.messageFunction = "sentItDirectMessage";
	
	// generates the JavaApplet embedding code and add it to the DOM tree 
	$('#appletContainer').html(deployJava.getAppletTag(attr, param, vers));
	
	// old generation code overwrites the hole DOM tree
	/* deployJava.runApplet(attr, getParameters(), vers); */
}

/**
 * Generates and shows a waiting progressbar.
 * This function is calles as 'action' (language file) on some dialogs
 */
function progressbarWaiting() {
	
	// generates a progressbar and sets its value to 100 percent
	$('#progressbarWaiting').progressbar({ value: 100 });
	
	// sets the background image of the progressbar with an animated image
	$('#progressbarWaiting').find('.ui-progressbar-value').css("background-image", "url('pbar-ani.gif')");
}

/**
 * Closes the current and/or the dialog from parameter
 * 
 * @param elem Dialog which should be closed
 */
function closeThisDialog(elem) {
	
	// close the passed in dialog
	$(elem).dialog('close');
	
	// close the current dialog
	$(this).dialog('close');
}

/**
 * Displays the 'info message' dialog with all information about send it direct
 */
function showInformations() {
	
	// close the old dialog fields
	$('.ui-dialog-content').dialog('close');
	
	// special option with bigger size, because there is much text in this dialog
	var dialogOptions = {
		
		width: 600,
		config: 'infoMessageDialog'
	};
	
	// displays the 'info message' dialog
	$('<div/>').dialog(dialogOptions);
}

/**
 * With this function messages are displayed in the browser.
 * If no type is set, there will be only displayed the message with an default ok button.
 * If a type is defined, the message will be ignored and the message and the buttons of the language file will be displayed.
 * This function is called by the JavaApplet if it has a message or an error occur.
 * 
 * @param message String with the message text
 * @param type String with the message type (here the text comes from language file)
 */
function sentItDirectMessage(message, type) {
	
	// set default dialog option, perhaps they will be overwritten by type settings
	// but if there is no type, these settings are needed for normal messages
	var dialogOptions = {
		
		buttons: { 'OK': closeThisDialog },
		title: '[' + type + '] Message'
	};
	
	// if a type is set, old dialogs must be closed
	// and the type must be set in dialog config
	if (type) {
		
		$('.ui-dialog-content').dialog('close');
		dialogOptions.config = type;
	}
	
	// if the type ends with 'Error' deaktivate preventUnload
	if (type.match("Error$") == "Error") preventUnload(false);
	
	// show the delivered message as dialog in the browser
	$('<div/>').html(message).dialog(dialogOptions);
}

/**
 * Function which had to be called when JavaApplet is started.
 * This function decides if the JavaApplet is used as client or as server application.
 * If the fileId is set, it will be work as client and receive the file;
 * If no fileId is set, it will be work as server and opens the file dialog for file selection.
 * This function will be called by the JavaApplet if it is started and ready.
 */
function sentItDirectAppletReady() {
	
	/**
	 * the timeout in this function is because some browsers are not realy ready (for example the firefox browser)
	 * without this timeout it can be happen that the call of the next function will fail
	 */
	
	// if no fileId is set, the JavaApplet works as server and opens a file dialog for file selection
	if (param.fid == '') {
		
		// prevents the unload of the page, because the JavaApplet will also be unloaded if the page will be unloaded
		preventUnload(true, "preventUnloadText");
		
		// open file dialog after 100 ms timeout
		window.setTimeout('openFileDialog()', 100);
	}
	// if a fileId is set, the JavaApplet works as client and has to start the download of the file (after 00 ms timeout)
	else window.setTimeout('startFileTransfer()', 100);
	
	// close the old dialog fields
	$('.ui-dialog-content').dialog('close');
}

/**
 * Opens the file dialog for file selection
 */
function openFileDialog() {
	
	// call the openFileDialog() method of the JavaApplet.
	// parameter is the callback function name where the selected file will be set
	document.sendItDirectApplet.openFileDialog('setFileSource');
}

/**
 * Displays the 'wait for connection' dialog and starts the file transfer.
 */
function startFileTransfer() {
	
	var dialogOptions = {
			
		config: 'waitForConnectionDialog'
	};
	
	// displays the 'wait for connection' dialog
	$('<div/>').dialog(dialogOptions);
	
	// call the startFileTransfer() method from the JavaApplet.
	// parameters are the fileId and the name of the callbackFunction
	document.sendItDirectApplet.startFileTransfer(param.fid, 'manageFileTranfser');
}

/**
 * This function starts the providing of the passed in file source.
 * This is realized by calling the provideFile() method of the JavaApplet.
 * 
 * @param fileSource
 */
function provideFile(fileSource) {
	
	// call the provideFile() method of the JavaApplet.
	// parameters are the path to the file source and the callback function name
	document.sendItDirectApplet.provideFile(fileSource, 'fileIsProvided');
}

/**
 * Function for file transfer management.
 * This function is called by the JavaApplet if the file can be received or the file transfer is finished
 * 
 * @param type String which contains the type (finished or url)
 * @param data String which contains the data if exist
 */
function manageFileTranfser(type, data) {
	
	// close the old dialog fields
	// TODO: ### Prüfen ob hier wirklich alte dialoge geschlossen werden müssen ###
	$('.ui-dialog-content').dialog('close');
	
	// if the type is 'finished' (file transfer finished) this will be displayed as dialog
	if (type == 'finished') {
		
		var dialogOptions = {
				
			config: 'fileTransferFinishedDialog'
		};
		
		// show dialog with 'file transfer finished' text
		$('<div/>').dialog(dialogOptions);
	}
	
	// if the type is 'url' the file transfer must be started
	if (type == 'url') {
		
		var dialogOptions = {
			
			config: 'waitFileDownloadDialog'
		};
		
		// display a dialog with download url for the case of no automatic start
		$('<div/>').dialog(dialogOptions).find('a').attr('href', data);
		
		// after a timeout of 500 ms start the download in browser
		// this method will be called after an timeout, because, if an error occur, it will be in JavaScript.
		// if there is no timeout, an error will fall back to the JavaApplet, because this had called this method.
		// additionally if the JavaScript starts the download, its better for security in some browsers.
		window.setTimeout('startDownload("' + data + '")', 500);
	}
}

/**
 * Starts die file download in browser and displays the 'start file download' dialog
 * 
 * @param url String with contains the URL for download
 */
function startDownload(url) {
	
	// close the old dialog fields
	$('.ui-dialog-content').dialog('close');
	
	// open the URL for download in the browser
	window.location.href = url;
	
	var dialogOptions = {
		
		config: 'startFileDownloadDialog'
	};
	
	// display the 'start file download' dialog
	$('<div/>').dialog(dialogOptions);
}

/**
 * Function sets the selected file source and calls the provideFile method in the JavaApplet.
 * This Function is called by the JavaApplet after the selection of an file.
 * 
 * @param fileSource String
 */
function setFileSource(fileSource) {
	
	// if no file source is selected, display the 'no file selected' dialogs
	if (fileSource == null) {
		
		var dialogOptions = { config: 'noFileMessageDialog' };
		
		$('<div/>').dialog(dialogOptions);
	}
	// else display the 'wait for file providing' dialog and call the provideFile method.
	// this method will be called after an timeout, because, if an error occur, it will be in JavaScript.
	// if there is no timeout, an error will fall back to the JavaApplet, because this had called this method.
	else {
		
		// prepare string for method call
		var provideFileCall = 'provideFile("' + addslashes(fileSource) + '")';
		
		var dialogOptions = {
			
			config: 'waitFileProvidingDialog'
		};
		
		// display 'wait for file providing' dialog
		$('<div/>').dialog(dialogOptions);
		
		// after timeout, call the provideFile method on JavaApplet
		window.setTimeout(provideFileCall, 100);
	}
}

/**
 * Adds slashes to special characters of the string in the parameter.
 * Special characters are backslashes, double quotes and single quotes.
 * 
 * @param str String with special characters which must be escaped
 * @returns String with escaped special characters
 */
function addslashes(str) {
	
	// add slashes and return the new string
    return (str + '').replace(/[\\"']/g, '\\$&');
}

/**
 * Closes the current dialog amd opens a new file dialog for file selection
 */
function selectFileFunction() {
	
	// close the current dialog
	closeThisDialog(this);
	
	// open the file dialog for file selection
	openFileDialog();
}

/**
 * Aborts the current file providing and opens a new file dialog for starting a new one.
 * This function is called by a button action of the language files
 */
function abortAndStartNew() {
	
	// close the old dialog fields
	$('.ui-dialog-content').dialog('close');
	
	// set the transfer state to false
	$('#transfer').data('exists', false);
	
	// call the abortFileProviding() method on the JavaApplet to abort the current file providing
	document.sendItDirectApplet.abortFileProviding();
	
	// open a new file dialog
	openFileDialog();
}

/**
 * Sets the value for the fileId, displays the 'file provided' dialog and sets a new callback method in the JavaApplet;
 * This callback method is for setting the transfer state of the provided file which Id is passed in here.
 * This Method will be called from JavaApplet after the file is provided 
 * 
 * @param fileId Unique ID of the provided file
 */
function fileIsProvided(fileId) {
	
	// close the old dialog fields
	$('.ui-dialog-content').dialog('close');
	
	// set the fileId in memory for later use
	$('#transfer').data('fileId', fileId);
	
	var dialogOptions = {
			
		config: 'transferStateDialog'
	};
	
	// open a new 'transfer state' dialog
	$('<div/>').dialog(dialogOptions);
	
	// set the callback method to 'setTransferState' which will be call after download start
	document.sendItDirectApplet.setCallbackFunction('setTransferState');
}

/**
 * Sets the URL where the file can be received and sets the 'wait for transfer' text.
 * This function is call by the 'action' (lang file) of the 'transferState' dialog
 */
function setAccessUrl() {
	
	// read the fileId out of the memory
	var fileId = $('#transfer').data('fileId');
	
	// set the URL for file download
	$('#fileAccessUrl').val(serviceURLBase + fileId);
	
	// set the 'wait for transfer' text in the dialog field
	$('#transferState').val($('#lang').data('waitForTransferText'));
}

/**
 * Sets the transfer state of a file transfered to a specific user.
 * This Method will be called from JavaApplet and display the file transfer state 
 * 
 * @param userId Unique ID of a downloading user
 * @param percentString Percent value of the file downloading progress
 * @param seconds Seconds left of the file downloading progress
 * @param speed Current speed of the file downloading progress 
 */
function setTransferState(userId, percentString, seconds, speed) {
	
	var information = "";
	var percent = parseInt(percentString);
	
	// If the current user has no progressbar, it must be generated
	if ($('#' + userId).length == 0) {
		
		// add a new div element with the userId to the transferState element
		$('#transferState').append('<div id="' + userId + '" />');
	}
	
	// Get the progressbar of the current user
	var statbar = $('#' + userId);
	
	// Set the progressbar with the current percent value of the file transfer 
	statbar.progressbar({ value: percent });
	
	// Generate an information string with additional file transfer information
	if (percent == 100) information = $('#lang').data('transferCompleteText');
	else if (percent == 0) information = $('#lang').data('waitForTransferText');
	else {
		
		var remain = $('#lang').data('remainingText');
		var speedt = $('#lang').data('speedText');
		
		information = percent + ' % [' + remain + ': ' + seconds + '][' + speedt + ': ' + speed + ' kB/s]';
	}
	
	// Remove the old information text of the current progressbar
	statbar.find('span[class=pbpos]').remove();
	
	// Add the new information text to the current progressbar
	statbar.append('<span class="pbpos">' + information + '<span>');
}

/**
 * Calls the copyToClipboard() Method of the JavaApplet.
 * This Method is called by a button-click and copies the content of a textfield.
 */
function copyToClipboard() {
	
	// call the copyToClipboard() method with die 'fileAccessUrl'
	document.sendItDirectApplet.copyToClipboard($('#fileAccessUrl').val());
}

/**
 * Interprets URL and retruns parameter object for JavaApplet.
 * Interpreted parameters: fid, mediatorIP, mediatorPort
 * 
 * @returns {Object JavaApplet parameters}
 */
function getParameters() {
	
	// unset the default fileId
	var fid = '';
	
	// Reads the parameters of the URL and overwrites the standard values 
	if (window.location.search != "") {
		
		// get the hole URL with parameter and split it to URL and parameters 
		var query = window.location.search.split("?");
		
		// split the parameters to single parameter in a array
		var param = query[1].split("&");
		
		// traverse each parameter
		for (var i = 0; i < param.length; ++i) {
			
			// split the parameter to its key and value part
			var keyval = param[i].split("=");
			
			// check if the key is the fileId (fid), mediatorIP or mediatorPort
			// if one of them is located, set the corresponding variable
			if (keyval[0] == "fid") fid = keyval[1];
			if (keyval[0] == "mediatorIP") mediatorIP = keyval[1];
			if (keyval[0] == "mediatorPort") mediatorPort = keyval[1];
		}
	}
	
	// returns an object with all neccessary information for the JavaApplet
	return { "mediatorIP": mediatorIP, "mediatorPort": mediatorPort, "fid": fid };
}
