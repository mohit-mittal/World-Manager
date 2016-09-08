var util_table1 = $('#util_table1'); // This Table shows 180 GPUs of 60 gpus (First Table)
var util_table2 = $('#util_table2'); // This Tabes shows 192 GPUs of 24 gpus (Second Table)

function get_util_Data() {
            var getButton = "get_util_button";
            var reload = "get_util_Data";
                var run = main(getButton, util_table1, util_table2, reload);    //calls main(). It is defined in include.js
    $(".container-util").empty();
}
get_util_Data();

function display_util(val) {
	return val.toFixed(2).toString() + metric_units['util'];
}

function range_util(val) {
        if (val == 0) {
            retval = "btn-default";
        } else
        if (val > 0 && val <= 25) {
           retval = "btn-light-green";
        } else
        if (val > 25 && val <= 50) {
            retval = "btn-spring-green";
        } else
        if (val > 50 && val <= 75) {
            retval = "btn-lime-green";
        } else
        if (val > 75 && val <= 100) {
            retval = "btn-dark-green";
        } else {
            retval = "btn-black";
        }
	return retval;
}
	
function get_util_button(i, item) {
	
    var retval = create_button_instance(item,"gpu"+i,"util",range_util,display_util);
    return retval;
    
}
