////////////////////////////////////////////////////////////////////////////////
// Input bindings
////////////////////////////////////////////////////////////////////////////////

ko.bindingHandlers.numberInput = {
	init : function(element, valueAccessor) {
		$(element).on("keydown", function(event) {
			// Allow: backspace, delete, tab, escape, and enter
			if (event.keyCode == 46 || event.keyCode == 8 || event.keyCode == 9 || event.keyCode == 27 || event.keyCode == 13 ||
			// Allow: Ctrl+A
			(event.keyCode == 65 && event.ctrlKey === true) ||
			// Allow: . ,
			(event.keyCode == 188 || event.keyCode == 190 || event.keyCode == 110) ||
			// Allow: home, end, left, right
			(event.keyCode >= 35 && event.keyCode <= 39)) {
				// let it happen, don't do anything
				return;
			} else {
				// Ensure that it is a number and stop the keypress
				if (event.shiftKey || (event.keyCode < 48 || event.keyCode > 57) && (event.keyCode < 96 || event.keyCode > 105)) {
					event.preventDefault();
				}
			}
		});
	}
};

ko.bindingHandlers.date = {
	init : function(element, valueAccessor) {
		// attach an event handler to our dom element to handle user input
		element.onchange = function() {
			var value = valueAccessor();
			// set our observable to the parsed date from the input
			value(moment(element.value).toDate());
		};
	},
	update : function(element, valueAccessor, allBindingsAccessor, viewModel) {
		var value = valueAccessor();
		var valueUnwrapped = ko.utils.unwrapObservable(value);
		var newValueAccessor = function() {
			return valueUnwrapped ? moment(valueUnwrapped).format('l LT') : '';
		};
		ko.bindingHandlers.text.update(element, newValueAccessor);
	}
};

ko.bindingHandlers.summernote = {
	init : function(element, valueAccessor, allBindingsAccessor) {
		var options = valueAccessor() || {};
		var value = options.value;
		delete options.value;
		var updateObservable = function(e) {
			var html = $(element).code();
			if (html !== value()) {
				value(html);
			}
		};
		options.onkeydown = options.onkeyup = options.onfocus = options.onblur = updateObservable;
		if (options.height == null || options.height == 0) {
			delete options.height;
		} else {
			options.height = options.height || $(element).height();
		}
		$(element).summernote(options);
	},
	update : function(element, valueAccessor) {
		var options = valueAccessor() || {};
		var value = options.value;
		var valueUnwrapped = ko.utils.unwrapObservable(value);
		if (valueUnwrapped !== $(element).code()) {
			$(element).code(valueUnwrapped);
		}
	}
};

ko.bindingHandlers.dropfile = {
	init: function (element, valueAccessor) {
		var value = ko.utils.unwrapObservable(valueAccessor());
		if (!value) {
			value = {};
		}
		if (value.name === undefined) {
			value.name = ko.observable();
		}
		if (value.size === undefined) {
			value.size = ko.observable();
		}
		if (value.type === undefined) {
			value.type = ko.observable();
		}
		if (value.data === undefined) {
			value.data = ko.observable();
		}

		// Check for FileReader or Silverlight
		if (!window.dropfile) {
			element.innerHTML = "Unfortunately this browser doesn't support FileReader or the shim. " + (!Silverlight.isInstalled() ? "<br/><b><a href='https://www.microsoft.com/getsilverlight/'>Please install Silverlight to upload images</a></b>" : "");
		}

		element.ondragover = function () {
			return false;
		};
		element.ondragenter = function () {
			return false;
		};
		element.ondrop = function (e) {

			// ensure that we listen out for the window event
			e = e || window.event;

			// And that for the fix to work we accept `e.files`
			var files = (e.files || e.dataTransfer.files)
				, file = files[0];

			if (!file) {
				if (ko.isObservable(value.name)) {
					value.name(null);
				} else {
					value.name = null;
				}
				if (ko.isObservable(value.size)) {
					value.size(null);
				} else {
					value.size = null;
				}
				if (ko.isObservable(value.type)) {
					value.type(null);
				} else {
					value.type = null;
				}
				if (ko.isObservable(value.data)) {
					value.data(null);
				} else {
					value.data = null;
				}
			} else {
				if (ko.isObservable(value.name)) {
					value.name(file.name);
				} else {
					value.name = file.name;
				}
				if (ko.isObservable(value.size)) {
					value.size(file.size);
				} else {
					value.size = file.size;
				}
				if (ko.isObservable(value.type)) {
					value.type(file.type);
				} else {
					value.type = file.type;
				}
				var reader = new FileReader();
				reader.onload = function (e) {
					if (ko.isObservable(value.data)) {
						value.data(e.target.result.substring(e.target.result.indexOf(",") + 1));
					} else {
						value.data = e.target.result.substring(e.target.result.indexOf(",") + 1);
					}
				};
				reader.readAsDataURL(file);
			}

			this.innerHTML = '<p>' + (file.name) + '</p>';

			if (page.fileSave)
				page.fileSave();


			return false;
		};

	}
};

////////////////////////////////////////////////////////////////////////////////
// Output bindings
////////////////////////////////////////////////////////////////////////////////

// Output an initial-cap version of the input as a read only text field

ko.bindingHandlers.enum = {
	update : function(element, valueAccessor) {
		var value = "" + ko.utils.unwrapObservable(valueAccessor());
		var newValueAccessor = function() {
			if (value.length <= 3) {
				return value;
			}
			return value.toLowerCase().replace(/_/g, ' ').replace(/(?:^|\s)\S/g, function(a) {
				return a.toUpperCase();
			});
		};
		ko.bindingHandlers.text.update(element, newValueAccessor);
	}
};

////////////////////////////////////////////////////////////////////////////////
// Specialized bindings
////////////////////////////////////////////////////////////////////////////////

// File Upload - for upload, use OBSERVABLE.dataURL().substring(OBSERVABLE.dataURL().indexOf(",") + 1) as the data source

ko.bindingHandlers.file = {
	init : function(element, valueAccessor, allBindingsAccessor) {
		var $element = $(element);
		var value = valueAccessor();
		value.binaryString = ko.observable();
		value.text = ko.observable();
		value.arrayBuffer = ko.observable();
		value.dataURL = ko.observable();
		$element.change(function() {
			var file = this.files[0];
			if (ko.isObservable(value)) {
				value(file);
			}
		});
	},
	update : function(element, valueAccessor, allBindingsAccessor) {
		var value = valueAccessor();
		var file = ko.utils.unwrapObservable(value);
		function read(type) {
			var reader = new FileReader();
			reader.onload = function(e) {
				value[type](e.target.result);
			}
			reader['readAs' + type[0].toUpperCase() + type.slice(1, type.length)](file);
		}
		if (!file) {
			value.binaryString(null);
			value.text(null);
			value.arrayBuffer(null);
			value.dataURL(null);
		} else {
			read('binaryString');
			read('text');
			read('arrayBuffer');
			read('dataURL');
		}
	}
};

// Google Maps

ko.bindingHandlers.googlemap = {
	init : function(element, valueAccessor) {
		var value = valueAccessor(), 
			location = value.location ? ko.utils.unwrapObservable(value.location) : value, 
			latLng = new google.maps.LatLng(ko.utils.unwrapObservable(location.latitude), ko.utils.unwrapObservable(location.longitude)), 
			disableDefaultUI = ko.utils.unwrapObservable(value.disableDefaultUI) ? true : false, 
			mapOptions = {
				zoom : 15,
				center : latLng,
				mapTypeId : google.maps.MapTypeId.ROADMAP,
				disableDefaultUI : disableDefaultUI
			}, 
			map = new google.maps.Map(element, mapOptions), 
			marker = new google.maps.Marker({
				position : latLng,
				map : map
			});
		}
};

// JSignature

ko.bindingHandlers.signature = { 
	init: function(element, valueAccessor) {
		var value = valueAccessor();
		$(element).empty();
		$(element).jSignature({ 
			width: $(element).width(), 
			height: $(element).height() ? $(element).height() : $(element).width / 3,
			color: $(element).css("color"),
			'background-color': '#fff',
			'decor-color': '#eee'
		});
		$(element).bind('change', function(e) {
			value($(element).jSignature("getData","base30")[1]);
		}); 			
	},
	update: function(element, valueAccessor) {
		var value = valueAccessor();
		var valueUnwrapped = ko.utils.unwrapObservable(value);
		$(element).jSignature("setData", "data:image/jsignature;base30," + valueUnwrapped);
	}
};

ko.bindingHandlers.signatureView = {
	update : function(element, valueAccessor) {
		var value = valueAccessor();
		var valueUnwrapped = ko.utils.unwrapObservable(value);
		var jsigdiv = $("<div></div>");
		var width = $(element).width();
		var height = $(element).height();
		if (!height) {
			height = width / 3;
		}
		jsigdiv.appendTo(element);
		jsigdiv.jSignature({
			width : width,
			height : height,
			color : $(element).css("color")
		});
		jsigdiv.jSignature("setData", "data:image/jsignature;base30," + valueUnwrapped);
		var datapair = jsigdiv.jSignature("getData", "svgbase64");
		$(element).empty();
		$('<img/>', {
			width : width,
			height : height,
			src : "data:" + datapair[0] + "," + datapair[1]
		}).appendTo(element);
	}
};

