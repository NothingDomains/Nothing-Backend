$(function() {



function openRegisterDialog() {
	vex.dialog.open({
		message: 'Please enter your info to register:',
		input: '' +
		'<input name="username" type="text" placeholder="Username" required />' +
		'<input name="email" type="text" placeholder="Email" required />' +
		'<input name="password" type="password" placeholder="Password" required />' +
		'<input name="confirmpassword" type="password" placeholder="Confirm Password" required />' +
		'<a href="/forgot">Forgot your password?</a>' +
		'',
		buttons: [
			$.extend({}, vex.dialog.buttons.YES, {text: 'Register'}),
			$.extend({}, vex.dialog.buttons.NO, {text: 'Cancel'})
		],
		callback: function (data) {
			if (!data) {
				window.pathname = "/";
				return;
			}
			var jsdata = {
				password: data.password,
				name: data.username,
				email: data.email
			};

			if (data.username === "" || data.username === null) {
				alert("You must specify a username!");
				return;
			}

			if (data.email === "" || data.email === null) {
				alert("You must specify an email!");
				return;
			}

			if (data.password === data.confirmpassword) {
				$.post({
					url: '/api/register',
					contentType: 'application/json',
					data: JSON.stringify(jsdata),
					complete: function (r) {
						console.log(r);
						if (r.status === 200) {
							vex.dialog.alert('You will get a verification email shortly! Make sure to check your Junk/Spam boxes');
							location.reload();
						} else {
							vex.dialog.alert(r.responseText);
						}
					}
				});
			} else vex.dialog.alert('Passwords don\'t match!');
		}
	});
}

$("a.register-button").click(function () {
	openRegisterDialog();
});

$("a.pure-button-primary").click(function () {
	openRegisterDialog();
});

$("a.login-button").click(function () {
	vex.dialog.open({
		message: 'Please enter your username and password:',
		input: '' +
		'<input name="username" type="text" placeholder="Username" required />' +
		'<input name="password" type="password" placeholder="Password" required />' +
		'',
		buttons: [
			$.extend({}, vex.dialog.buttons.YES, {text: 'Login'}),
			$.extend({}, vex.dialog.buttons.NO, {text: 'Cancel'})
		],
		callback: function (data) {
			if (!data) {
				window.pathname = "/";
			} else {
				var jsdata = {
					password: data.password,
					user: data.username
				};
				$.post({
					url: '/api/login',
					contentType: 'application/json',
					data: JSON.stringify(jsdata),
					complete: function (r) {
						if (r.status === 200)
							location.href = "/client-area";
						else
							vex.dialog.alert(r.responseText);
					}
				});
			}
		}
	});
});
});