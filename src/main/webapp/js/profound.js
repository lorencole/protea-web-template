var Profound = Profound || {};

Profound.App = function() {

	var self = this;

	self.rebind = function(container) {
		var element = container ? container : document.body;
		ko.cleanNode(element);
		ko.applyBindings(self, element);
	};
	
	self.getAuthorizationHeader = function() {
		var token = localStorage.authorizationToken + ':';
		var hashed = btoa(token);
		return "Basic " + hashed;
	};

	self.getAjaxRequest = function(type, url, data, async, successCallback, errorCallback) {
		return {
			type: type,
			url: url,
			data: data,
			async: async,
			dataType: 'json',
			beforeSend: function (xhr) { 
		        xhr.setRequestHeader('Authorization', self.getAuthorizationHeader()); 
		    },
			error: function (jqXHR, textStatus, errorThrown) {
				if (jqXHR.status in this.statusCode) {
					return;
				}
				if (errorCallback) {
					errorCallback(jqXHR.responseJSON);
				} else if (successCallback) {
					successCallback(jqXHR.responseJSON);
				}
			},
			statusCode: {
				401: function() {
					window.location = 'login.html';
				}
			},
			success: function(a, b, c) {
				successCallback(a, b, c);
			},
			contentType: 'application/json'
		}
	};
	
	self.ajaxString = function(type, url, data, successCallback, errorCallback) {
		var request = self.getAjaxRequest(type, url, data, true, successCallback, errorCallback);
		request.dataType = 'text';
		$.ajax(request);
	};
	
	self.ajax = function(type, url, data, successCallback, errorCallback) {
		var request = self.getAjaxRequest(type, url, data, true, successCallback, errorCallback);
		$.ajax(request);
	};
	
	self.initializing = ko.observable(true);
	
	self.me = ko.observable();
	
	if (! localStorage.authorizationToken) {
		self.initializing(false);
	}
	
	self.me = ko.computedObservable(function() {
		
	});
	
	self.syncAjax('GET', '/api/user/current', null, function(data) {
		if (!data) {
			document.location = 'login.html';
		}
		self.user(data);
		if (! localStorage.organizationKey) {
			self.organizationKey(self.user().currentOrganizationKey);
		} else {
			self.organizationKey(localStorage.organizationKey);
		}
		$('body').css('visibility', 'visible');
	}, function(request, status, error) {
		var url = 'login.html';
		if (error.messge) {
			url += '?message=' + encode(error.message); 
		}
		document.location = url;
	});

	
	localStorage.removeItem('authorizationToken');
}

Profound.Util = Profound.Util || {};

Profound.Util.loadURL = function(url, container){
	pageLocalScript = null;
	container
		.hide()
		.html('<h1><i class="fa fa-cog fa-spin"></i> Loading...</h1>')
		.load(url, function(response, status, xhr) {
			if ( status == "error" ) {
				container.html('<h4 style="margin-top:10px; display:block; text-align:left"><i class="fa fa-warning txt-color-orangeDark"></i> Error 404! Page not found.</h4>');
				return;
			}
			app.rebind(this);
			Profound.Util.onNewPageFragment();
			if (pageLocalScript) {
				pageLocalScript();
			}
		    window.onresize();
		})
		.fadeIn('fast');
}

Profound.Util.captureQueryParameters = function() {
	window.queryParameters = {};
	if (location.hash.indexOf("?") != -1) {
		location.hash.replace( /^#/, '' ).replace( /.*\?/, '').replace(/([^&=]+)=?([^&]*)(?:&+|$)/g, function(match, key, value) {
	        (window.queryParameters[key] = window.queryParameters[key] || []).push(value);
	    });
	}
}

Profound.Util.onNewPageFragment = function() {
	$('table.fix-header').floatThead({ 
		scrollContainer: function(t) { return t.parent(); } 
	});
}

window.onresize = function() {
	if (window.page && window.page.onresize) {
		window.page.onresize();
	}
};


