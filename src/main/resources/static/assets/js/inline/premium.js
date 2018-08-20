$(function() {


	console.log("loaded.");

	$("input.submit").click(function () {
		var jsdata = {
			type: $("#recordtype").val(),
			name: $("#name").val(),
			target: $("#target").val(),
			domain: $("#domain").val(),
			proxied: $("#proxied").is(":checked")
		};

		$.post({
			url: '/api/premium/dns/create',
			contentType: 'application/json',
			data: JSON.stringify(jsdata),
			complete: function (r) {
				if (r.status === 200) {
					vex.dialog.alert("Your record has been added.");
				} else {
					vex.dialog.alert(r.responseJSON.error);
				}
			}
		});

	});


function makeEmail() {
	$.get({
		url: '/api/premium/email/create',
		complete: function (r) {
			if (r.status === 200) {
				var email = r.responseJSON.username;
				var password = r.responseJSON.password;
				$("#email").html('<p>Your email is:' + email + ' and your password is: ' + password + '</p>' + '<p>You can login at: <a href="https://email.nothing.domains/mail">https://email.nothing.domains/mail</a>. Please use your full email as your username.');
			} else {
				vex.dialog.alert(r.responseText);
			}
		}
	});
}
});