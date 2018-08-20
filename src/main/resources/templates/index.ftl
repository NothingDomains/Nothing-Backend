<!doctype html>
<html lang="en">
<head>
	<meta charset="utf-8">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<meta name="description" content="Nothing Domains - Free file hosting with custom domains">
	<meta property="og:title" content="Nothing Domains"/>
	<meta property="og:url" content="https://nothing.domains"/>
	<meta property="og:description" content="Free file hosting with custom domains"/>
	<meta property="og:image" content="/assets/nothinglogo.png"/>
	<title>Home // Nothing</title>
	<script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/3.2.1/jquery.min.js" integrity="sha256-hwg4gsxgFZhOsEEamdOYGBf13FyQuiTwlAQgxVSNgt4=" crossorigin="anonymous"></script>
	<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/pure/1.0.0/pure-min.css" integrity="sha256-Q0zCrUs2IfXWYx0uMKJfG93CvF6oVII21waYsAV4/8Q=" crossorigin="anonymous"/>
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
	<script src="assets/js/piwik.js"></script>
	<script src="assets/js/inline/index.js"></script>


</head>
<body>

<div class="header">
	<div class="home-menu pure-menu pure-menu-horizontal pure-menu-fixed">
		<a class="pure-menu-heading" href="/">Nothing Domains</a>

		<ul class="pure-menu-list">
		<#if user == "">
			<li class="pure-menu-item pure-menu-selected"><a href="/" class="pure-menu-link">Home</a></li>
			<li class="pure-menu-item"><a href="/tutorial" class="pure-menu-link">Tutorial</a></li>
			<li class="pure-menu-item"><a class="pure-menu-link login-button">Sign In</a></li>
			<li class="pure-menu-item"><a class="pure-menu-link register-button">Register</a></li>
		<#else>
			<li class="pure-menu-item">Welcome, ${name}</li>
			<li class="pure-menu-item pure-menu-selected"><a href="/" class="pure-menu-link">Home</a></li>
			<li class="pure-menu-item"><a href="/tutorial" class="pure-menu-link">Tutorial</a></li>
			<li class="pure-menu-item"><a href="/client-area" class="pure-menu-link">Client Area</a></li>
			<li class="pure-menu-item"><a href="/shorten" class="pure-menu-link">Shorten URL</a></li>
			<li class="pure-menu-item"><a href="/premium" class="pure-menu-link">Premium</a></li>
			<li class="pure-menu-item"><a href="/api/login/logout" class="pure-menu-link">Logout</a></li>
		</#if>
		</ul>
	</div>
</div>

<div class="splash-container">
	<div class="splash">
		<h1 class="splash-head">Nothing Domains</h1>
		<p class="splash-subhead">
			Hello! We're Nothing Domains. We provide <b>file hosting</b>, <b>image hosting</b>, <b>url shortening</b>
			and other services for free...
		</p>
		<p>
			<a class="pure-button pure-button-primary">Get Started</a>
		</p>
	</div>
</div>

<div class="content-wrapper">
	<div class="content">
		<h2 class="content-head is-center">Features</h2>

		<div class="pure-g">
			<div class="l-box pure-u-1 pure-u-md-1-2 pure-u-lg-1-3">

				<h3 class="content-subhead">
					<i class="fa fa-globe"></i>
					Custom Domains
				</h3>
				<p>
					We offer tons of custom domains to make your image link look cool! Some of our domains include: <b>dm-sli.de</b>,
					<b>*.dabs-on.me</b>, and <b>*.discordnitro.com</b> (full list below)
				</p>
			</div>
			<div class="l-box pure-u-1 pure-u-md-1-2 pure-u-lg-1-3">
				<h3 class="content-subhead">
					<i class="fa fa-mobile"></i>
					Fast Support
				</h3>
				<p>
					If you have any questions, just ask in our <a href="https://discord.gg/7tqU98E">Discord server</a>,
					where we can quickly
					resolve any issues you may be having, and answer your questions.
				</p>
			</div>
			<div class="l-box pure-u-1 pure-u-md-1-2 pure-u-lg-1-3">
				<h3 class="content-subhead">
					<i class="fa fa-th-large"></i>
					Free
				</h3>
				<p>
					Yup, completely free, as in free beer. All image hosting, file hosting, and url shortening is
					provided completely free with no strings attached. We promise we won't sell or farm your data.
				</p>
			</div>
		</div>
	</div>

	<div class="ribbon l-box-lrg pure-g">
		<div class="l-box-lrg is-center pure-u-1 pure-u-md-1-2 pure-u-lg-2-5">
			<img width="300" alt="File Icons" class="pure-img-responsive" src="assets/img/common/star.png">
		</div>
		<div class="pure-u-1 pure-u-md-1-2 pure-u-lg-3-5">

			<h2 class="content-head content-head-ribbon">Premium</h2>

			<p>
				We provide this service for free, so to cover our costs we allow Premium users some extra benefits!
				Premium users are allowed to have <b>2 DNS Records</b>(ex. i-like-cow.dm-sli.de pointing to your server)
				on our service, as well as an email address(you@oneofourdomains.com)!
				Premium only costs $5 a month, and you can get it via PayPal in the client area.
			</p>
		</div>
	</div>

	<div class="content">
		<div id="domain-list" class="content-head is-center">Domain List</div>
		<div class="pure-g" style="text-align:center;">
			<div class="pure-u-1">
				<ul style="list-style: none; padding-left:0;">
				<#list domains as domain>
					<li>${domain}</li>
				</#list>
				</ul>
			</div>
		</div>
	</div>
	<div style="text-align: center;">
		<p>Copyright © Nothing Domains 2017</p>
		<a href="/assets/tos.txt">Terms of Service</a>
		<a href="/assets/privacypolicy.txt">Privacy Policy</a>
	</div>
</div>
</body>
</html>
