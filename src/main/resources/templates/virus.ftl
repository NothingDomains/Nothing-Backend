<!doctype html>
<html lang="en">
<head>
	<meta charset="utf-8">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<meta name="description" content="Nothing Domains - Free file hosting with custom domains">
	<meta property="og:title" content="Virus // Nothing Domains"/>
	<meta property="og:image" content="/assets/nothinglogo.png"/>
	<meta property="og:description" content="Free file hosting with custom domains"/>
	<title>Message from our dolphins // Nothing</title>
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
	<link rel="stylesheet" href="/assets/css/layouts/marketing-old-ie.css">
	<![endif]-->
	<!--[if gt IE 8]><!-->
	<link rel="stylesheet" href="/assets/css/layouts/marketing.css">
	<!--<![endif]-->

	<!-- Locally hosted files -->
	<script src="/assets/js/vex.combined.min.js"></script>
	<link rel="stylesheet" href="/assets/css/vex.css"/>
	<link rel="stylesheet" href="/assets/css/vex-theme-os.css"/>
	<script>vex.defaultOptions.className = 'vex-theme-os'</script>
</head>
<body>
<div class="header">
	<div class="home-menu pure-menu pure-menu-horizontal pure-menu-fixed">
		<a class="pure-menu-heading" href="/">Nothing Domains</a>

		<ul class="pure-menu-list">
			<li class="pure-menu-item pure-menu-selected"><a href="/" class="pure-menu-link">Home</a></li>
		</ul>
	</div>
</div>
<div class="splash-container">
	<div class="splash">
		<h2 class="splash-head">Our dolphins found a virus!</h2>
		<p class="splash-subhead">
			List of viruses found:
		</p>
	</div>
</div>
<div class="content-wrapper">
	<div class="content">
		<div class="">
			<ul>
			<#list wiruses as virus>
				<li>${virus}</li>
			</#list>
			</ul>
		</div>
	</div>
</div>
</body>
</html>
