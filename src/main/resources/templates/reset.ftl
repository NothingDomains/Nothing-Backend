<!doctype html>
<html lang="en">
<head>
	<meta charset="utf-8">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<meta name="description" content="Reset your password">
	<title>Nothing // Reset Password</title>
	<!-- CDNs -->
	<script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/3.2.1/jquery.min.js" integrity="sha256-hwg4gsxgFZhOsEEamdOYGBf13FyQuiTwlAQgxVSNgt4=" crossorigin="anonymous"></script>
	<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/pure/1.0.0/pure-min.css" integrity="sha256-Q0zCrUs2IfXWYx0uMKJfG93CvF6oVII21waYsAV4/8Q=" crossorigin="anonymous" />
	<script src="https://use.fontawesome.com/0400668a3b.js"></script>
	<!--[if lte IE 8]>
	<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/pure/1.0.0/grids-responsive-old-ie-min.css" />
	<![endif]-->
	<!--[if gt IE 8]><!-->
	<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/pure/1.0.0/grids-responsive-min.css" />
	<!--<![endif]-->
	<!--[if lte IE 8]>
	<link rel="stylesheet" href="assets/css/layouts/marketing-old-ie.css">
	<![endif]-->
	<!--[if gt IE 8]><!-->
	<link rel="stylesheet" href="assets/css/layouts/marketing.css">
	<!--<![endif]-->

	<!-- Locally hosted files -->
	<script src="assets/js/vex.combined.min.js"></script>
	<link rel="stylesheet" href="assets/css/vex.css"/>
	<link rel="stylesheet" href="assets/css/vex-theme-os.css"/>
	<script>vex.defaultOptions.className = 'vex-theme-os'</script>
</head>
<body>

<div class="header">
	<div class="home-menu pure-menu pure-menu-horizontal pure-menu-fixed">
		<a class="pure-menu-heading" href="/">Nothing Domains</a>
		<ul class="pure-menu-list">
			<li class="pure-menu-item pure-menu-selected"><a href="/" class="pure-menu-link">Home</a></li>
			<li class="pure-menu-item"><a href="/tutorial" class="pure-menu-link">Tutorial</a></li>
			<li class="pure-menu-item"><a class="pure-menu-link login-button">Sign In</a></li>
			<li class="pure-menu-item"><a class="pure-menu-link register-button">Register</a></li>
		</ul>
	</div>
</div>

<div class="splash-container">
	<div class="splash">
		<h1>Password Reset</h1>
		<div class="pure-form">
			<input type="password" name="password" placeholder="Password" id="password" required>
			<input type="password" name="confirmpassword" placeholder="Confirm Password" id="confirmpassword" required>
			<input type="submit" name="submit" class="submit pure-button">
		</div>
	</div>
</div>
</body>
<script type="text/javascript">
	$('input.submit').click(function () {
		var pw = $('#password');
		if (pw.val() === $('#confirmpassword').val()) {
			$.post({
				url: '/api/login/reset/${token}',
				data: pw.val(),
				contentType: 'text/plain',
				complete: function (r) {
					console.log(r);
					if (r.status === 200)
						location.pathname = "/";
					else
						alert(r.responseText);
					location.pathname = "/";
				}
			});
		} else {
			vex.dialog.alert("Your passwords don't match!")
		}
	});
</script>
</html>
