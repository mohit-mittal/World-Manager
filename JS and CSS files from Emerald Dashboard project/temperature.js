var temp_table1 = $('#temp_table1'); // This Table shows 180 GPUs of 60 gpus (First Table)
var temp_table2 = $('#temp_table2'); // This Tabes shows 192 GPUs of 24 gpus (Second Table)

function get_temp_Data() {
                var getButton = "get_temp_button";
            var reload = "get_temp_Data";
            var run = main(getButton, temp_table1, temp_table2, reload);    //calls main(). It is defined in include.js

    $(".container-temp").empty();
}
get_temp_Data();

function display_temp(val) {
	return val.toFixed(2).toString() + metric_units['temp'];
}

function range_temp(val) {

    if (val >= 18 && val <= 30) {
        retval = "btn-primary";
    } else
    if (val > 30 && val <= 40) {
        retval = "btn-success";
    } else
    if (val > 40 && val <= 50) {
        retval = "btn-warning";
    } else
    if (val > 50) {
        retval = "btn-danger";
    } else {
        retval = "btn-black";
    }
    
    return retval;
}
 
function get_temp_button(i, item) {
	
    var retval = create_button_instance(item,"gpu"+i,"temp",range_temp,display_temp);
    return retval;
    
}