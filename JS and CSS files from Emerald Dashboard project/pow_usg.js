var power_usg_table1 = $('#power_usg_table1'); // This Table shows 180 GPUs of 60 hosts (First Table)
var power_usg_table2 = $('#power_usg_table2'); // This Tabes shows 192 GPUs of 24 hosts (Second Table)

function get_pow_usg_Data() {
            var getButton = "get_pow_usg_button";
            var reload = "get_pow_usg_Data";
            var run = main(getButton, power_usg_table1, power_usg_table2, reload);  //calls main(). It is defined in include.js

    $(".container-pow-usg").empty();
}
get_pow_usg_Data();

function display_pow_usage(val) {
	return val.toFixed(2).toString() + metric_units['power_usage'];
}

function range_pow_usage(val) {
        if (val >= 0 && val <= 70) {
            retval = "btn-primary";
        } else
        if (val > 70 && val <= 130) {
            retval = "btn-success";
        } else
        if (val > 130 && val <= 190) {
            retval = "btn-warning";
        } else
        if (val > 190 && val <= 250) {
            retval = "btn-danger";
        } else {
            retval = "btn-black";
        }	
	return retval;
}

function get_pow_usg_button(i, item) {
	
    var retval = create_button_instance(item,"gpu"+i,"power_usage",range_pow_usage,display_pow_usage);
    return retval;
    
}

