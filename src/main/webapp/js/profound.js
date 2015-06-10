var Profound = Profound || {};

Profound.Util = Profound.Util || {};

Profound.Util.setHash = function(hash) {
	if (! hash || hash.length == 0) {
		return;
	}
	if (hash.substring(0, 1) != '#') {
		hash = '#' + hash;
	}
	if (window.location.hash && window.location.hash == hash) {
		window.onhashchange();
	} else {
		window.location.hash = hash;
	}
};

Profound.Util.loadURL = function(url, container){
	pageLocalScript = null;
	container
		.hide()
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
		.show()
		;
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

window.onhashchange = function() {
	console.log("In onhashchange");
	var url = app.me() ? 'authenticated/' : 'unknown/';
	var hash = location.hash.replace( /^#/, '' ).replace( /\?.*/, '');
	if (hash == null || hash == undefined || hash.length == 0) {
		return;
	}
	url += hash;
	console.log("Changing inner page to " + url);
	var container = $('#profoundInnerPage');
	if (container.length == 1) {
		console.log("Hashchange : " + url);
		Profound.Util.loadURL(url, container);
	}
	window.queryParameters = {};
	if (location.hash.indexOf("?") != -1) {
		location.hash.replace( /^#/, '' ).replace( /.*\?/, '').replace(/([^&=]+)=?([^&]*)(?:&+|$)/g, function(match, key, value) {
	        (window.queryParameters[key] = window.queryParameters[key] || []).push(value);
	    });
	}
};

Profound.App = function() {

	var self = this;
	
	self.pages = {};

	self.rebind = function(container) {
		var element = container ? container : document.body;
		ko.cleanNode(element);
		ko.applyBindings(self, element);
	};
	
	self.getAuthorizationHeader = function() {
		if (! localStorage.sessionToken) {
			return "";
		}
		var token = localStorage.sessionToken + ':';
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
				if (jqXHR.status in this.statusCode && (jqXHR.status != 401 || localStorage.sessionToken)) {
					return;
				}
				if (errorCallback) {
					errorCallback(jqXHR.responseJSON);
				} else if (successCallback) {
					successCallback(jqXHR.responseJSON);
				}
			},
			statusCode: {
				401: function(jqXHR, textStatus, errorThrown) {
					if (! localStorage.sessionToken) {
						window.hash = 'login.html';
					} else if (errorCallback) {
						errorCallback(jqXHR.responseJSON);
					} else if (successCallback) {
						successCallback(jqXHR.responseJSON);
					}
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
	
	self.syncAjax = function(type, url, data, successCallback, errorCallback) {
		var request = self.getAjaxRequest(type, url, data, false, successCallback, errorCallback);
		$.ajax(request);
	};
	
	self.me = ko.observable();
	self.me.subscribe(function(newValue) {
		if (newValue != null) {
			if (window.onlogin) {
				window.onlogin();
			}
		} else {
			if (window.onlogout) {
				window.onlogout();
			}
		}
	});
	
	self.reloadCurrentUser = function() {
		if (! localStorage.sessionToken) {
			self.me(null);
			return;
		}
		console.log("Loading current user for session token " + localStorage.sessionToken);
		self.syncAjax('GET', '/api/users/current', null, function(data) {
			if (data) {
				if (! data.name) {
					data.name = data.firstName + ' ' + data.lastName;
				}
			}
			self.me(data);
			console.log("Current user loaded");
			if (window.onlogin) {
				console.log("Calling window.onlogin");
				window.onlogin();
			}
		}, function(error) {
			self.me(null);
			console.log("Could not access current user");
			localStorage.removeItem('sessionToken');
			if (window.onlogout) {
				window.onlogout();
			} else {
				if (window.location.hash) {
					window.onhashchange();
				}
			}
		});
	};
	
	self.knownUser = function() {
		if (! self.me()) {
			return false;
		}
		// TODO validate email &c
		return true;
	};
	
	self.initialize = function() {
		if (! localStorage.sessionToken) {
			console.log("No session token");
			if (window.onlogout) {
				window.onlogout();
			} else {
				if (window.location.hash) {
					window.onhashchange();
				}
			}
		} else {
			self.reloadCurrentUser();
		}
		$('body').css('visibility', 'visible');			
	};

	self.logout = function() {
		app.ajax('POST', '/api/sessions/logout', null, function(response) {
			if (response && response.success) {
				app.me(null);
				localStorage.removeItem('sessionToken');
				if (window.onlogout) {
					window.onlogout();
				} else {
					if (window.location.hash) {
						window.onhashchange();
					}
				}
			}
		});
	};
}

hello.init( Profound.Config.Social , {
	redirect_uri : '/hello/redirect.html',
	oauth_proxy  : '/helloshim'
});

hello.on('auth.login', function(auth) {
	if (! localStorage.sessionToken) {
		var data = {};
		var key = auth.network + 'AccessToken';
		data[key] = auth.authResponse.access_token;
		app.syncAjax('POST', '/api/sessions/login', JSON.stringify(data), function(response) {
			if (response && response.sessionToken) {
				localStorage.sessionToken = response.sessionToken;
			}
			app.reloadCurrentUser();
		});
	} else {
		// TODO add token to the current user?
	}	
});
