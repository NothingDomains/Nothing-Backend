<!doctype html>
<html lang="en">
<head>
	<meta charset="utf-8">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<meta name="description" content="Tutorial">
	<title>Tutorial // Nothing</title>
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

		pre.code {
			background-color: #061d2d;
			color: white;
			border: #135886 1px solid;
			text-align: left;
			font-family: monospace;
			border-radius: 15px;
			padding: 1%;
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
		<h1>Nothing Domains - Tutorial</h1>
	</div>
	<div style="align-content: center; alignment: center; text-align: center; display: inline-block">
		<h2 id="sharex">ShareX Setup</h2>
		<p>Step 1. Open ShareX > Destinations > Destination Settings</p>
		<img src="assets/img/tutorial/sharex/Step1.png" alt="ShareX Step 1" style="max-width: 500px;">
		<br>
		<br>
		<p>Step 2. Copy the JSON code below to your clipboard. Go to "Custom Uploaders" in ShareX > Import > From
			Clipboard</p>
		<pre class="code">
<code>{
	"Name": "Nothing.Domains",
	"RequestType": "POST",
	"RequestURL": "https://nothing.domains/api/upload/pomf",
	"FileFormName": "files[]",
	"Headers": {
		"Authorization": "API KEY HERE"
	},
	"ResponseType": "Text",
	"URL": "https://domain you want/$json:files[0].url$"
}</code></pre>
		<br>
		<img src="assets/img/tutorial/sharex/Step2.png" alt="ShareX Step 2" style="max-width: 500px;">
		<br>
		<br>
		<p>Step 3. Replace "API KEY HERE" in Headers with an API key from your client area. Then replace "domain you
			want" (and only that text) in the URL with the domain you want supported by us.</p>
		<img src="assets/img/tutorial/sharex/Step3.png" alt="ShareX Step 3" style="max-width: 500px;">
		<br>
		<br>
		<p>Step 4. Confirm and select the nothing domains uploader. If you did everything correctly, uploading files
			should work without issue!</p>
		<img src="assets/img/tutorial/sharex/Step4.png" alt="ShareX Step 4" style="max-width: 500px;">
	</div>
	<footer class="is-center mg-top-xl">Made by Arsen, Ponce, and Binner</footer>
</div>
</body>
</html>
