/** 
 * This detects when mouse enter in any slide, it calls the popover and activates it.
 */
$(document).hover(function () {
    $("button").popover({
        trigger: "hover",
        placement: get_popover_placement,
        html: true
    });
});

/**
 * Hash tables from metrics to their display units and names
 */

var metric_units = new Object();
metric_units['temp'] = '°C';
metric_units['util'] = '%';
metric_units['power_usage'] = 'W';
metric_units['run_proc'] = '';

var metric_name = new Object();
metric_name['temp'] = 'Temperature';
metric_name['util'] = 'Utilisation';
metric_name['power_usage'] = 'Power Usage';
metric_name['run_proc'] = 'Running Process';


function get_detailed_info(obj, metric) {
    var retval = "<table class = 'modal_value'>";

    if (metric == 'temp') {
        temp_value();
        util_value();
        power_usage_value();
        run_proc_value();
    }
    if (metric == 'util') {
        util_value();
        power_usage_value();
        run_proc_value();
        temp_value();
    }
    if (metric == 'power_usage') {
        power_usage_value();
        run_proc_value();
        temp_value();
        util_value();
    }
    if (metric == 'run_proc') {
        run_proc_value();
        temp_value();
        util_value();
        power_usage_value();
    }

    function power_usage_value() {
        if (typeof (obj['power_usage']) == "object") {
            retval += "<tr><td><h4>" + metric_name["power_usage"] + "</h4></td><td><h4>: No Value</h4></td></tr>";
        } else {
            retval += "<tr><td><h4>" + metric_name["power_usage"] + "</h4></td><td><h4>: " + obj['power_usage'].toFixed(2) + metric_units["power_usage"] + "</h4></td></tr>";
        }
    }

    function run_proc_value() {
        if (typeof (obj['run_proc']) == "object") {
            retval += "<tr><td><h4>" + metric_name["run_proc"] + "</h4></td><td><h4>: No Value</h4></td></tr>";
        } else {
            retval += "<tr><td><h4>" + metric_name["run_proc"] + "</h4></td><td><h4>: " + obj['run_proc'] + metric_units["run_proc"] + "</h4></td></tr>";
        }
    }

    function temp_value() {
        if (typeof (obj['temp']) == "object") {
            retval += "<tr><td><h4>" + metric_name["temp"] + "</h4></td><td><h4>: No Value</h4></td></tr>";
        } else {
            retval += "<tr><td><h4>" + metric_name["temp"] + "</h4></td><td><h4>: " + obj['temp'] + metric_units["temp"] + "</h4></td></tr>";
        }
    }

    function util_value() {
        if (typeof (obj['util']) == "object") {
            retval += "<tr><td><h4>" + metric_name["util"] + "</h4></td><td><h4>: No Value</h4></td></tr>";
        } else {
            retval += "<tr><td><h4>" + metric_name["util"] + "</h4></td><td><h4>: " + obj['util'].toFixed(2) + metric_units["util"] + "</h4></td></tr>";
        }
    }
    retval += "</table>";
    return retval;

}

//This JQuery listener will get the data from the modal open button when its clicked and pass it to the modal to display or use
$(document).on("click", ".open-InfoModal", function () {
    // button = $('button').removeClass('btn-sm').addClass('btn-lg');
    var mydata = $(this).data('id');
    $(".modal-body").html(mydata);
    var hostname = $(this).data('text');
    $('.modal-title').html(hostname);
    $('#popOverHostname').on("click", function () {
        var hostn = $("#popOverHostname").text(); // gets the modal title text.
        // var mtitle = $(".modal-title").text();  // gets the modal title text.
        // var hostn = mtitle.replace(/,.*/, "");  // removes ", GPU*" from hostname
        // if($('.modal-title').text()!="What is this?") // Checks if it's about's modal?
        window.open("https://www.emerald.rl.ac.uk/ganglia/?c=GPU%20Cluster&h=" + hostn);
    });
});

/**
 * A generic create button function
 * Takes the host json object and current gpu and metric, plus util_value() function pointers and returns the button
 */

function returnPOContent(mName, dVal, hostname, gpuWithNumber) {
    if (mName.indexOf('Temp') >= 0) {
        var shortMetric = "temp";
        var unit = "celcius";
    } else if (mName.indexOf('Util') >= 0) {
        var shortMetric = "util";
        var unit = "%";
    } else if (mName.indexOf('Power') >= 0) {
        var shortMetric = "power_usage";
        var unit = "milliwatts";
    } else if (mName.indexOf('Running') >= 0) {
        var shortMetric = "run_proc";
        var unit = "processes";
    }
    var gpuNumber = gpuWithNumber.slice(-1);
    var popOverContent = mName + ": " + dVal + "</br><div id='test'><img src='https://www.emerald.rl.ac.uk/ganglia/graph.php?r=day&z=small&c=GPU+Cluster&h=" + hostname + "&v=" + dVal + "&m=gpu" + gpuNumber + "_" + shortMetric + "&jr=&js=&vl=" + unit + "&ti=GPU+" + gpuNumber + "+" + mName + "'/></div>";
    return popOverContent;
}

function create_button_instance(obj, gpu, metric, rangefn, displayfn) {
    var metric_val = obj[gpu][metric];
    var detailed_summary = get_detailed_info(obj[gpu], metric);
    var display_val;
    if (typeof (metric_val) == "object") {
        display_val = "No&#32;Value";
        metric_val = -1;
    } else {
        display_val = displayfn(metric_val);
    }

    var button_state = rangefn(metric_val);
    var button = "<button title = " + obj.hostname + "&#44;&#32;" + gpu.toUpperCase() +
        " data-content = \"" + returnPOContent(metric_name[metric], display_val, obj.hostname, gpu) + "\"" +
        " data-id=\"" + detailed_summary + "\"" +
        " data-text = \"" + returnPOTitle(obj.hostname, gpu.toUpperCase()) + "\"" +
        " class=\"btn " + button_state + " gpu btn-lg open-InfoModal\"" +
        " data-toggle=\"modal\" " +
        " data-html=\"true\" " +
        " rel=\"popover\" " +
        " data-target=\"#hostInfo\" " +
        " href=\"#infoModal\"></button>";
    return button;
}


function returnPOTitle(hostname, gpu) {
    var popOverTitle = "<div id = 'popOverHostname'>" + hostname + "</div><div id = 'popOvergpu'> , " + gpu + "</div>";
    return popOverTitle;
}
/**
 * This function returns the popover placement.
 * It automatically detects the windows width and
 * returns the suitable place for the popover accordingly
 */

function get_popover_placement(pop, dom_el) {
    var width = window.innerWidth;
    if (width < 500) return 'bottom';
    var left_pos = $(dom_el).offset().left;
    if (width - left_pos > 450) return 'right';
    return 'left';
}

/**
 * I have initialised and configured bxslider here.
 * You can add more options to it, according to your requirements.
 * Here is the link <http://bxslider.com/options> for more options.
 */

$(window).load(function () {
    var slider = $('.bxslider').bxSlider({
        // Auto show will pause when mouse hovers over slider
        autoHover: true,
        // Slides will automatically transition
        auto: true,
        // Dynamically adjust slider height based on each slide's height        
        adaptiveHeight: true,
        // Enable or disable auto resize of the slider.       
        responsive: true,
        // Slide transition duration (in ms). (1000ms = 1sec)
        speed: 1500,
        // If true, "Start" / "Stop" controls will be added 
        autoControls: true,
        // The amount of time (in ms) between each auto transition. (1000ms = 1sec)
        pause: 8000,
        // Type of transition between slides
        mode: 'fade',
        // If true, a pager will be added
        pager: true,
        // Using a Custom Pager for this (bxslider) slider, link <http://bxslider.com/examples/thumbnail-pager-1>
        pagerCustom: '#bx-pager',

    });

});

function main(getButton, table1, table2, reloadfunction) {
    var gpu3host = new RegExp("^cn3g");
    var shorthost_reg = new RegExp("^cn.g..");

    $.getJSON("new_json_file.json", function (data) {
        var count = 0;
        var counter = 0;
        var i;
        var row_tab = $('<tr></tr>').addClass('test');
        table1.append(row_tab);
        table2.append(row_tab);

        $.each(data, function (i, item) {

            var c = item.hostname;
            var cls;
            // Assign class based on hostname regexp
            if (gpu3host.test(item.hostname)) {
                cls = "host3";
                gpus = 3;
            } else {
                cls = "host8";
                gpus = 8;
            }
            var shorthost = shorthost_reg.exec(item.hostname);
            var buttonrow = getButtonRow(item, getButton);
            if (gpus == 3) {
                count = count + 3;
                var cell_tab = getcell(buttonrow);
                row_tab.append(cell_tab);

                if ((count % 30) == 0) {
                    table1.append(row_tab);
                    row_tab = $('<tr></tr>').addClass('test');
                }

            } else if (gpus == 8) {
                counter = counter + 8;
                cell_tab = getcell(buttonrow);
                row_tab.append(cell_tab);

                if ((counter % 32) == 0) {
                    table2.append(row_tab);
                    row_tab = $('<tr></tr>').addClass('test');
                }

            }

        });
        // console.log(eval(reloadfunction));
    });
    setTimeout(window[reloadfunction], 180000); //time is 180 seconds (180000ms)

}

function getButtonRow(item, getButton) {
    var buttonrow = "<div class=\"shadow\" id = \"shadow\">";

    for (i = 0; i < gpus; i++) {
        //Create a button and set its data to the host id
        buttonrow = buttonrow + window[getButton](i, item);

    }
    buttonrow = buttonrow + "</div>";
    return buttonrow;
}

function getcell(buttonrow) {
    var cell = $('<td style="text-align:center"></td>').addClass('test').wrapInner(buttonrow);
    return cell;
}
$(document).ready(function () {

    $('#about').on("click", function () {
        $(".modal-dialog").removeClass("modal-sm");
        $('.modal-dialog').addClass('modal-lg');
        $("#hostInfo").modal('show');
        $(".modal-title").html("What is this?");
        $(".modal-body").html("<p class = 'abouttext question'>How to use the dashboard?</p> <p class = 'abouttext answer'>Each square represents one GPU in the Centre for Innovation GPU Cluster Emerald, grouped together by the host that contains them. Hover over a GPU to display the current value for the current metric. Click on a GPU to display all the metrics for the current GPU.</p> <p class = 'abouttext answer'> Click the name of the metric at the bottom of the screen to jump directly to that metric, alternatively use the arrows buttons to rotate through the metrics. Use the play and pause buttons in the bottom right to start or stop the automatic transition of metrics.</p> <p class = 'abouttext question'>How is the data collected? </p> <p class = 'abouttext answer'>The metrics are collected using the nvidia management library NVML. These are aggregated by an instance of the Ganglia cluster monitoring system, A custom script queries the RRD files used by ganglia to extract the latest values and produces a JSON file.</p> <p class = 'abouttext question'> How is the data displayed? What tools we have used? </p> <p class = 'abouttext answer'> Data is displayed in the form of Bootstrap Buttons having different colours. We have used JQuery v1.10.2 with <a href = 'http://getbootstrap.com/' target='_blank'>Twitter Bootstrap v3</a> for Buttons, Popovers, Pager, and Modal. For different tabs and slides, we have used <a href = 'http://bxslider.com/' target = '_blank'>bxslider</a> </p>  ");

        return false;
    });

});

$(document).click(function () {
    $('#hostInfo').on('hidden.bs.modal', function (e) {
        $(".modal-dialog").removeClass("modal-lg");
        $('.modal-dialog').addClass('modal-sm');
    });
});