$(document).ready(function() {
	if (typeof isDfrAddedOrRemoved !== 'undefined' && !isDfrAddedOrRemoved && totalDfrsAvailable == totalDfrsConfigured)
		{
		$('[id^="btnConfigure"]').val("Edit Configuration");
		$('[id^="btnConfigure"]').prop("name","action:editStationDetails"); // Changing the action using name attribute
		$('[id^="btnSend"]').prop("disabled",false);
		}
	else
		{
		$('[id^="btnConfigure"]').val("Save & Edit Configuration");
		$('[id^="btnConfigure"]').prop("name","action:saveNewlyAddedDfrsAction"); // Changing the action using name attribute
		$('[id^="btnSend"]').prop("disabled",true);
		}
	
});

function confirmBefore(isNewlyAdded, idButton)
{
        event.preventDefault();
        if (!isNewlyAdded)
        	{
				if (confirm("This will remove this dfr with its configuration. Do you want to continue to delete?"))
				{
				        console.log("Before submit topic..."+idButton);
				        $('#'+idButton).click();
				}
        	}
        else
        	{
        	$('#'+idButton).click();
        	}
}

function change_image(id, imageName)
{
	var imgTag = "<img src='images/dfr"+imageName+".jpg'/>";
	document.getElementById('imageDiv'+id).innerHTML = imgTag;
}

function showBusyCursor()
{
        $("*").css("cursor", "progress");
}
