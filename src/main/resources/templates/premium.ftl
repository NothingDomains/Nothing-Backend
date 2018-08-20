<!doctype html>
<html lang="en">
<head>
	<meta charset="utf-8">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<meta name="description" content="Client Area">
	<title>Premium // Nothing</title>
	<!-- CDNs -->
	<script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/3.2.1/jquery.min.js" integrity="sha256-hwg4gsxgFZhOsEEamdOYGBf13FyQuiTwlAQgxVSNgt4=" crossorigin="anonymous"></script>
	<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/pure/1.0.0/pure-min.css" integrity="sha256-Q0zCrUs2IfXWYx0uMKJfG93CvF6oVII21waYsAV4/8Q=" crossorigin="anonymous" />
	<script src="https://use.fontawesome.com/0400668a3b.js"></script>
	<!--[if lte IE 8]>
	<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/pure/1.0.0/grids-responsive-old-ie-min.css" />
	<![endif]-->
	<!--[if gt IE 8]><!-->
	<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/pure/1.0.0/grids-responsive-min.css" />
	<link rel="stylesheet" href="assets/css/layouts/marketing-premium.css">


	<!-- Locally hosted files -->
	<script type="text/javascript" src="assets/js/jquery.chocolat.js"></script>
	<script src="assets/js/vex.combined.min.js"></script>
	<script src="assets/js/jquery-paginate.min.js"></script>
	<link rel="stylesheet" href="assets/css/chocolat.css" type="text/css" media="screen" charset="utf-8">
	<link rel="stylesheet" href="assets/css/vex.css"/>
	<link rel="stylesheet" href="assets/css/vex-theme-os.css"/>
	<script>vex.defaultOptions.className = 'vex-theme-os'</script>
	<script src="assets/js/piwik.js"></script>

	<#if premium>
	    <script src="assets/js/inline/premium.js"></script>
	</#if>

	<style>
	.checkbox-left {
    width: auto !important;
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
			<li class="pure-menu-item"><a href="/tutorial" class="pure-menu-link">Tutorial</a></li>
			<li class="pure-menu-item"><a class="pure-menu-link login-button">Sign In</a></li>
			<li class="pure-menu-item"><a class="pure-menu-link register-button">Register</a></li>
		<#else>
			<li class="pure-menu-item">Welcome, ${name}</li>
			<li class="pure-menu-item"><a href="/" class="pure-menu-link">Home</a></li>
			<li class="pure-menu-item"><a href="/tutorial" class="pure-menu-link">Tutorial</a></li>
			<li class="pure-menu-item"><a href="/client-area" class="pure-menu-link">Client Area</a></li>
			<li class="pure-menu-item"><a href="/shorten" class="pure-menu-link">Shorten URL</a></li>
			<li class="pure-menu-item pure-menu-selected"><a href="/premium" class="pure-menu-link">Premium</a></li>
			<li class="pure-menu-item"><a href="/api/login/logout" class="pure-menu-link">Logout</a></li>
		</#if>

		</ul>
	</div>
</div>

<div class="content content-wrapper">
	<div class="mg-top-xl is-center">
		<h1>Nothing Domains - Premium</h1>
	</div>

	<section class="cs">
		<#if premium>
		<#if emailExists>
		<p>Your email address is: ${name}@nothing.domains, and you can login <a href="https://email.nothing.domains/mail">here</a>. If you need your password reset, ask in the #general chat on Discord.</p>
		<#else>
		<div id="email">
		<p>Welcome to Premium! It looks as if you haven't created your email address. Please click <b><a onclick="makeEmail()">here</a></b> to do it.</p>
		<p>Please note: You will NEED to copy down the password that appears, as it will disappear when you reload.</p>
		</div>
		</#if>
			<div class="record-add-form pure-form">
				<p>Record Type</p>
				<select id="recordtype" name="type" required>
					<option value="A">A</option>
					<option value="CNAME">CNAME</option>
				</select>
				<p>Record Name (Existing records will error!)</p>
				<input type="text" id="name" name="name" placeholder="${name}" required>
				<p>Record Target (Hostname for CNAME, IP for A)</p>
				<input type="text" id="target" name="target" placeholder="127.0.0.1" required>
				<p>Record Zone</p>
				<select id="domain" name="domain" required>
					<#list domains as domain>
						<option value="${domain}">${domain}</option>
					</#list>
				</select>
				<p>Proxied Through Cloudflare?</p>
				<input type="checkbox" class="checkbox-left" id="proxied" name="proxied" value="proxied">
				<br>
				<input type="submit" name="submit" class="submit pure-button" value="Submit">
			</div>
			<p>Please Note: Currently there is no way to remove or update records without contacting us through Discord. This will change soon.</p>
			<p>Current number of Records: ${recordCount}</p>
		<#else>
		<div id="buy-premium">
			<center>
				<h2>Hi! It seems that you are not a Premium member.</h2>
				<h3>Premium is a paid part of Nothing Domains which allows you increased upload limits, an email address, as well as DNS records on our domains.</h3>
				<p>Premium costs $3 a month, and you can purchase it by clicking this button:</p>
				<form action="https://www.paypal.com/cgi-bin/webscr" method="post" target="_top">
					<input type="hidden" name="cmd" value="_s-xclick">
					<input type="hidden" name="custom" value="${uuid}">
					<input type="hidden" name="hosted_button_id" value="AYKGYYLDVT9YW">
					<input type="image" src="https://www.paypalobjects.com/en_US/GB/i/btn/btn_subscribeCC_LG.gif" border="0" name="submit" alt="PayPal – The safer, easier way to pay online!">
					<img alt="" border="0" src="https://www.paypalobjects.com/en_GB/i/scr/pixel.gif" width="1" height="1">
				</form>
			</center>

		</div>
		</#if>
	</section>

	<footer class="is-center mg-top-xl">Made by Arsen, Ponce, and Binner</footer>
</div>
</body>
</html>
