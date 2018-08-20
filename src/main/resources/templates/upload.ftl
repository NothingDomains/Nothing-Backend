<!doctype html>
<html lang="en">
<head>
	<meta charset="utf-8">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<meta name="description" content="Tutorial">
	<title>Upload // Nothing</title>
	<!-- CDNs -->
	<script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/3.2.1/jquery.min.js"
			integrity="sha256-hwg4gsxgFZhOsEEamdOYGBf13FyQuiTwlAQgxVSNgt4=" crossorigin="anonymous"></script>
	<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/pure/1.0.0/pure-min.css"
		  integrity="sha256-Q0zCrUs2IfXWYx0uMKJfG93CvF6oVII21waYsAV4/8Q=" crossorigin="anonymous"/>
	<script src="https://use.fontawesome.com/0400668a3b.js"></script>
	<!--[if lte IE 8]>
	<link rel="stylesheet" href="https://unpkg.com/purecss@1.0.0/build/grids-responsive-old-ie-min.css">
	<![endif]-->
	<!--[if gt IE 8]><!-->
	<link rel="stylesheet" href="https://unpkg.com/purecss@1.0.0/build/grids-responsive-min.css">
	<link rel="stylesheet" href="assets/css/layouts/marketing-client-area.css">


	<!-- Locally hosted files -->
	<script type="text/javascript" src="assets/js/jquery.chocolat.js"></script>
	<script src="assets/js/vex.combined.min.js"></script>
	<script src="assets/js/jquery-paginate.min.js"></script>
	<link rel="stylesheet" href="assets/css/chocolat.css" type="text/css" media="screen" charset="utf-8">
	<link rel="stylesheet" href="assets/css/vex.css"/>
	<link rel="stylesheet" href="assets/css/vex-theme-os.css"/>
	<script>vex.defaultOptions.className = 'vex-theme-os'</script>
	<script src="assets/js/piwik.js"></script>

	<style>
		.page-navigation a {
			margin: 5px 2px;
			display: inline-block;
			padding: 1px 10px;
			color: #ffffff;
			background-color: #2d3e50;
			border: 1px solid #2d3e50;
			border-radius: 2px;
			text-decoration: none;
			font-weight: bold;
			font-size: 12px;
		}

		.page-navigation a[data-selected] {
			background-color: #0f7ac1;
		}

		body {
			text-align: center;
			align-content: center;
		}
	</style>
</head>
<body>

<div class="header">
	<div class="home-menu pure-menu pure-menu-horizontal pure-menu-fixed">
		<a class="pure-menu-heading" href="/">Nothing Domains</a>

		<ul class="pure-menu-list">

		<#if user == "">
			<li class="pure-menu-item"><a href="/" class="pure-menu-link">Home</a></li>
			<li class="pure-menu-item pure-menu-selected"><a href="/tutorial" class="pure-menu-link">Tutorial</a></li>
			<li class="pure-menu-item"><a class="pure-menu-link login-button">Sign In</a></li>
			<li class="pure-menu-item"><a class="pure-menu-link register-button">Register</a></li>
		<#else>
			<li class="pure-menu-item">Welcome, ${name}</li>
			<li class="pure-menu-item"><a href="/" class="pure-menu-link">Home</a></li>
			<li class="pure-menu-item pure-menu-selected"><a href="/tutorial" class="pure-menu-link">Tutorial</a></li>
			<li class="pure-menu-item"><a href="/client-area" class="pure-menu-link">Client Area</a></li>
			<li class="pure-menu-item"><a href="/shorten" class="pure-menu-link">Shorten URL</a></li>
			<li class="pure-menu-item"><a href="/premium" class="pure-menu-link">Premium</a></li>
			<li class="pure-menu-item"><a href="/api/login/logout" class="pure-menu-link">Logout</a></li>
		</#if>
		</ul>
	</div>
</div>

<div class="content content-wrapper">
	<div class="mg-top-xl is-center">
		<h1>Nothing Domains - Upload</h1>
	</div>
	<div style="align-content: center; alignment: center; text-align: center; display: inline-block">

		<form name="uploader" enctype="multipart/form-data" method="post" action="/api/upload/session">
			<label>File
				<br>
				<input type="file" name="file">
			</label>
			<br>
			<br>
			<input type="submit" value="Upload">
		</form>

	</div>
	<footer class="is-center mg-top-xl">Made by Arsen, Ponce, and Binner</footer>
</div>
</body>
</html>
