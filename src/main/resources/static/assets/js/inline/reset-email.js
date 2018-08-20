$(function() {



$('input.submit').click(function () {
	$.get({
		url: '/api/login/reset/' + $('.email').val(),
		contentType: 'application/json',
		complete: function (r) {
			console.log($('.email').val());
			console.log(r);
			if (r.status === 200)
				location.pathname = "/";
			else {
				alert(r.responseText);
				location.pathname = "/";
			}
		}
	});
});
});