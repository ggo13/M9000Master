
var scopeChart = $("#imgScopeChart"),

    intervalId = setInterval(

        function() {

        	scopeChart.attr("src",scopeChart.attr('src')+'?'+Math.random());

        },

        1000); // 300 sec === 5 min




$('#jfScopeDataRefresh').change(function() {

    if($(this).is(":checked")) {

   		intervalId = setInterval(

        function() {

        	scopeChart.attr("src",scopeChart.attr('src')+'?'+Math.random());

        },

        1000); // 300 sec === 5 min

   	}

   	else

   	{

   		clearInterval(intervalId);

   	}

   });