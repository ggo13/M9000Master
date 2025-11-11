  function handlePasteEvent(e)
  {
	  var cd = e.originalEvent.clipboardData;
	  var clipboardText;
	  if (cd == null) {
		  console.log("Inside if " + window.clipboardData);
		  cd = window.clipboardData;
		  clipboardText = cd.getData("text");
	  }
	  else {
		  clipboardText = cd.getData("text/plain");
	  }
	  var confirmed = true;
	  if (clipboardText == null || clipboardText.trim() == "") {
					  alert("No data available to paste");
					  return;
				  }
	  // if (clipboardText.indexOf("\n") != -1)
	  if (clipboardText.split(/\r\n|\r|\n/).length > 1) 
	  {
		  confirmed = confirm("About to copy multiple lines. This will overwrite the existing descriptions. Do you wish to continue?");
		  console.log("confirm? " + confirmed);
		  if (confirmed) {
			  console.log("Clipboard text " + clipboardText);
			  if (cd != null) {
				  if (clipboardText == null || clipboardText.trim() == "") {
					  alert("No data available to paste");
				  }
				  else {
					  console.log("Pasting..." + clipboardText + " from element " + e.target.id + " val " + e.target.value);
					  var destElementId = e.target.id;
					  pasteNewDescriptions(destElementId, clipboardText);
				  }
			  }
		  }

	  }
/*	  else {
		  var concatedText = "";
		  if (window.getSelection().toString() != "") {
			  concatedText = $(e.target).val().replace(window.getSelection(), clipboardText, document.activeElement.selectionStart, document.activeElement.selectionEnd);
			  console.log("IF Concated text with selection..." + concatedText);
		  }
		  else {
			  concatedText = $(e.target).val() + clipboardText;
			  console.log("ELSE Concated text with no selection..." + concatedText);
		  }
		  var objName = e.target.id;

		  if (concatedText.length <= 64) {
			  $(e.target).val(concatedText);
			  $(jqSelector(objName)).next('.error-message').text('');
		  }
		  else {
			  $(e.target).val(concatedText.substring(0, 64));
			  // $(jqSelector(objName)).next('.error-message').text('Description truncated to 64 chars as per standard'); 
			  $(jqSelector(objName)).next('.error-message').text('');
			  $(jqSelector(objName)).before("<span class='validationMessage' style='color:orange;font-weight:bold'>WARNING! Description truncated to 64 chars as per COMTRADE standard.</span>");
		  }

	  }
*/	  return;

}

// function to paste new descriptions when copied from excel
function pasteNewDescriptions(destElementId, copiedText) {
	var idIndex = destElementId.substring(destElementId.indexOf("[") + 1, destElementId.indexOf("]"));
	var objPrefix = destElementId.substring(0, destElementId.indexOf("[") + 1);
	var objSuffix = destElementId.substring(destElementId.indexOf("]"));
	console.log("element id " + destElementId + " Index of id " + idIndex + " prefix " + objPrefix + " Suffix " + objSuffix);
	var form = document.forms[0];
	var i = parseInt(idIndex);
	var objName = objPrefix + i + objSuffix;

	var arrText = copiedText.split("\n");
	var j = 0;
	var descText = "";
	console.log("objName " + objName + " arr size " + arrText.length);
	while (j < arrText.length && arrText[j] != '' && form.elements[objName] != null) {
		descText = arrText[j++].trim();
		if (descText != "") {
			if (descText.length <= 64) {
				form.elements[objName].value = descText;
	//			$(jqSelector(objName)).next('.error-message').text('');
			}
			else {
				form.elements[objName].value = descText.substring(0, 64);
				// $(jqSelector(objName)).next('.error-message').text('Description truncated to 64 chars as per standard');
//				$(jqSelector(objName)).before("<span class='validationMessage' style='color:orange;font-weight:bold'>WARNING! Text shortened to 64 chars per COMTRADE standard.</span>");
			}
			i = i + 1;
			objName = objPrefix + i + objSuffix;
		}
	}
}

// To select the element in jquery using java script element name
function jqSelector( id ) {
    return "#" + id.replace( /(:|\.|\[|\]|,)/g, "\\$1" );
}