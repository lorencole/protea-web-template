window.page = window.page || {};

var Profound = Profound || {};

Profound.Util = Profound.Util || {};

Profound.Util.setHash = function(hash) {
	if (! hash || hash.length == 0) {
		return;
	}
	if (hash.substring(0, 1) != '#') {
		hash = '#' + hash;
	}
	if (hash == '#') {
		return;
	}
	if (window.location.hash && window.location.hash == hash) {
		window.onhashchange();
	} else {
		window.location.hash = hash;
	}
};

Profound.Util.loadURL = function(url, container, redirect){
	window.pageLocalScript = null;
	url += (url.indexOf('?') == -1 ? '?' : ':') + '_=' + (new Date()).getTime();
	container
		.hide()
		.load(url, function(response, status) {
			if ( status == "error" ) {
				if (redirect) {
					Profound.Util.setHash('index.html');
				} else {
					container.html('<h4 style="margin-top:10px; display:block; text-align:left"><i class="fa fa-warning txt-color-orangeDark"></i> Error 404! Page not found.</h4>').show();
				}
				return;
			}
			container.show();
			app.rebind(this);
			Profound.Util.onNewPageFragment();

			if (pageLocalScript) {
				pageLocalScript();
			}
			if (window.onPageLoad) {
				window.onPageLoad();
			}
			window.onresize();
		});
	$('body').scrollTop(0);
};


Profound.Util.onNewPageFragment = function() {
	$('table.fix-header').floatThead({ 
		scrollContainer: function(t) { return t.parent(); } 
	});
};

window.onresize = function() {
	if (window.page && window.page.onresize) {
		window.page.onresize();
	}
};

window.onrefresh = function() {
	if (window.page && window.page.onrefresh) {
		window.page.onrefresh();
	}
};

window.refreshTimer = setInterval(window.onrefresh, 1000 * 60);

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
		Profound.Util.loadURL(url, container, window.redirect);
		window.redirect = false;
	}
	window.queryParameters = {};
	if (location.hash.indexOf("?") != -1) {
		location.hash.replace( /^#/, '' ).replace( /.*\?/, '').replace(/([^&=]+)=?([^&]*)(?:&+|$)/g, function(match, key, value) {
	        (window.queryParameters[key] = window.queryParameters[key] || []).push(value);
	    });
	}
	app.queryParameters(window.queryParameters);
};

Profound.App = function() {

	var self = this;
	
	self.pages = {};
	self.page = {};

	self.queryParameters = ko.observable();

	self.setSessionToken = function(token) {
		self.sessionToken = token;
		try {
			localStorage.sessionToken = token;
		} catch(err) {
			console.log(err);
		}
	};

	self.getSessionToken = function() {
		if (self.sessionToken) {
			return self.sessionToken;
		}
		return localStorage.sessionToken;
	};

	self.removeSessionToken = function() {
		delete self.sessionToken;
		try {
			localStorage.removeItem('sessionToken');
		} catch (err) {
			console.log(err);
		}
	};

	self.rebind = function(container) {
		var element = container ? container : document.body;
		ko.cleanNode(element);
		ko.applyBindings(self, element);
	};

	var Base64={_keyStr:"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=",encode:function(e){var t="";var n,r,i,s,o,u,a;var f=0;e=Base64._utf8_encode(e);while(f<e.length){n=e.charCodeAt(f++);r=e.charCodeAt(f++);i=e.charCodeAt(f++);s=n>>2;o=(n&3)<<4|r>>4;u=(r&15)<<2|i>>6;a=i&63;if(isNaN(r)){u=a=64}else if(isNaN(i)){a=64}t=t+this._keyStr.charAt(s)+this._keyStr.charAt(o)+this._keyStr.charAt(u)+this._keyStr.charAt(a)}return t},decode:function(e){var t="";var n,r,i;var s,o,u,a;var f=0;e=e.replace(/[^A-Za-z0-9\+\/\=]/g,"");while(f<e.length){s=this._keyStr.indexOf(e.charAt(f++));o=this._keyStr.indexOf(e.charAt(f++));u=this._keyStr.indexOf(e.charAt(f++));a=this._keyStr.indexOf(e.charAt(f++));n=s<<2|o>>4;r=(o&15)<<4|u>>2;i=(u&3)<<6|a;t=t+String.fromCharCode(n);if(u!=64){t=t+String.fromCharCode(r)}if(a!=64){t=t+String.fromCharCode(i)}}t=Base64._utf8_decode(t);return t},_utf8_encode:function(e){e=e.replace(/\r\n/g,"\n");var t="";for(var n=0;n<e.length;n++){var r=e.charCodeAt(n);if(r<128){t+=String.fromCharCode(r)}else if(r>127&&r<2048){t+=String.fromCharCode(r>>6|192);t+=String.fromCharCode(r&63|128)}else{t+=String.fromCharCode(r>>12|224);t+=String.fromCharCode(r>>6&63|128);t+=String.fromCharCode(r&63|128)}}return t},_utf8_decode:function(e){var t="";var n=0;var r=c1=c2=0;while(n<e.length){r=e.charCodeAt(n);if(r<128){t+=String.fromCharCode(r);n++}else if(r>191&&r<224){c2=e.charCodeAt(n+1);t+=String.fromCharCode((r&31)<<6|c2&63);n+=2}else{c2=e.charCodeAt(n+1);c3=e.charCodeAt(n+2);t+=String.fromCharCode((r&15)<<12|(c2&63)<<6|c3&63);n+=3}}return t}}

	self.getAuthorizationHeader = function(sessionToken) {
		if (! sessionToken) {
			sessionToken = self.getSessionToken();
		}
		if (! sessionToken) {
			return "";
		}
		var token = sessionToken + ':';
		var hashed;
		if (window.btoa) {
			hashed = window.btoa(token);
		} else {
			hashed = Base64.encode(token);
		}
		return "Basic " + hashed;
	};

	self.getAjaxRequest = function(type, url, data, async, successCallback, errorCallback) {
		return {
			type: type,
			url: url,
			data: data,
			cache: false,
			async: async,
			dataType: 'json',
			beforeSend: function (xhr) { 
		        xhr.setRequestHeader('Authorization', self.getAuthorizationHeader()); 
		    },
			error: function (jqXHR, textStatus, errorThrown) {
				if (jqXHR.status in this.statusCode && (jqXHR.status != 401 || self.getSessionToken())) {
					return;
				}
				if (errorCallback) {
					errorCallback(jqXHR.responseJSON, textStatus, errorThrown);
				} else if (successCallback) {
					successCallback(jqXHR.responseJSON);
				}
			},
			statusCode: {
				401: function(jqXHR, textStatus, errorThrown) {
					if (! self.getSessionToken()) {
						window.hash = 'login.html';
					} else if (errorCallback) {
						errorCallback(jqXHR.responseJSON);
					} else if (successCallback) {
						var ret = {};
						if (jqXHR && jqXHR.responseJSON) {
							ret = jqXHR.responseJSON;
						}
						if (! ret.success) {
							ret.success = false;
						}
						if (! ret.message) {
							ret.message = 'An unexpected error has occurred';
						}
						successCallback(ret);
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
		if (! self.getSessionToken()) {
			self.me(null);
			return;
		}
		console.log("Loading current user for session token " + self.getSessionToken());
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
			self.removeSessionToken();
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
		if (! self.getSessionToken()) {
			console.log("No session token");

			if(window.location.hash) {
				var hash = window.location.hash;
				if( hash.indexOf('?') > -1 ) {
					hash = hash.substr(0, hash.indexOf('?'));
				}

				// if hash is set to one of the "unknown" pages, don't redirect
				if( hash == '#login.html'
					|| 	hash == '#resetPassword.html'
					|| 	hash == '#sendPasswordEmail.html'
					|| 	hash == '#signUp.html'
					|| 	hash == '#validateEmail.html'
				) {
					window.onhashchange();
				}
				else if (window.onlogout) {
					// if hash does not match "unknown" pages, send to login screen
					window.onlogout();
				}
			}
			else if (window.onlogout) {
				// if no hash present, send to login screen
				window.onlogout();
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
				self.removeSessionToken();
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
	if (! app.getSessionToken()) {
		var data = {};
		var key = auth.network + 'AccessToken';
		data[key] = auth.authResponse.access_token;
		app.syncAjax('POST', '/api/sessions/login', JSON.stringify(data), function(response) {
			if (response && response.sessionToken) {
				app.setSessionToken(response.sessionToken);
			}
			app.reloadCurrentUser();
		});
	} else {
		// TODO add token to the current user?
	}	
});
