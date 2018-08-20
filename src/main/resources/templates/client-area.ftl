<!doctype html>
<html lang="en">
<head>
	<meta charset="utf-8">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<meta name="description" content="Client Area">
	<title>Client Area // Nothing</title>
	<!-- CDNs -->
	<script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/3.2.1/jquery.min.js" integrity="sha256-hwg4gsxgFZhOsEEamdOYGBf13FyQuiTwlAQgxVSNgt4=" crossorigin="anonymous"></script>
	<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/pure/1.0.0/pure-min.css" integrity="sha256-Q0zCrUs2IfXWYx0uMKJfG93CvF6oVII21waYsAV4/8Q=" crossorigin="anonymous" />
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
	<script src="assets/js/inline/client-area.js"></script>
	<script src="assets/js/piwik.js"></script>

	<style>
		.page-navigation {
			float: right;
		}

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

		a.chocolat-image {
			text-decoration: none;
			color: #fff;
			border-bottom: 1px dashed #4599f5;
			padding-bottom: 2px;
		}

		div.chocolat-overlay {
			background-color: #000000;
		}

	</style>

	<script>
		console.log("we out heya");
		var navigationHidden = false;
		function delimg(id) {
			$.get({
				url: '/api/upload/delete/' + id,
				success: function () {
					location.reload();
				}
			});
		}

		function closeTable() {
			$('table.toggle-table-table').toggle();
			if (navigationHidden === true) {
				navigationHidden = false;
				$('div.page-navigation').attr("style", "");
			} else {
				navigationHidden = true;
				$('div.page-navigation').attr("style", "display: none;");
			}

		}
		function delkey(key) {
			$.get({
				url: '/api/upload/keys/delete/' + key,
				success: function () {
					location.reload();
				}
			});
		}
		function addkey() {
			$.get({
				url: '/api/upload/keys/new',
				success: function () {
					location.reload();
				}
			});
		}

		$(function() {
			$('table.toggle-table-table').paginate({limit: 25});
			$('.chocolat-parent').Chocolat();
		});
	</script>


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
			<li class="pure-menu-item"><a href="/" class="pure-menu-link">Home</a></li>
			<li class="pure-menu-item"><a href="/tutorial" class="pure-menu-link">Tutorial</a></li>
			<li class="pure-menu-item pure-menu-selected"><a href="/client-area" class="pure-menu-link">Client Area</a></li>
			<li class="pure-menu-item"><a href="/shorten" class="pure-menu-link">Shorten URL</a></li>
			<li class="pure-menu-item"><a href="/premium" class="pure-menu-link">Premium</a></li>
			<li class="pure-menu-item"><a href="/api/login/logout" class="pure-menu-link">Logout</a></li>
		</#if>

		</ul>
	</div>
</div>

<div class="content content-wrapper">
	<div class="mg-top-xl is-center">
		<h1>Nothing Domains - Client Area</h1>
	</div>
	<section class="cs">
		<h2 class="cs-title">Uploads <a class="button-small pure-button toggle-table" onclick="closeTable()">Toggle
			Uploads Table</a></h2>
		<table class="cs-table mg-top pure-table pure-table-bordered toggle-table-table">
			<tr>
				<th>Hash</th>
				<th>Date Created</th>
				<th>Size</th>
				<th>URL</th>
				<th>Delete</th>
			</tr>
		<#list images as image>
			<tr>
				<td>
					<div class="chocolat-parent" data-chocolat-title="${image.hash}">
						<a class="chocolat-image" href="https://nothing.domains/${image.url}" title="${image.hash}">
						${image.hash}
						</a>
					</div>
				</td>
				<td>${image.date}</td>
				<td>${image.size}</td>
				<td><a class="cs-table-link button-small pure-button preview" target="_blank"
					   href="https://nothing.domains/${image.url}">Link</a></td>
				<td><a class="cs-table-link button-small pure-button" target="_blank" onclick="delimg('${image.url}')">Delete</a>
				</td>
			</tr>
		</#list>
		</table>
	</section>

	<section id="api_keys" class="cs">
		<h2 class="cs-title">API keys <a class="button-small pure-button" id="newkey" target="_blank" onclick="addkey()">New API Key</a></h2>
		<table id="keys" class="cs-table pure-table pure-table-bordered">
			<tr>
				<th>Key</th>
				<th>Usages</th>
				<th>Delete</th>
			</tr>
		<#list keys as key>
			<tr>
				<td>${key.value}</td>
				<td>${key.usages}</td>
				<td><a class="cs-table-link button-small pure-button" target="_blank" onclick="delkey('${key.value}')">Delete
					Key</a></td>
			</tr>
		</#list>
		</table>
	</section>

	<footer class="is-center mg-top-xl">Made by Arsen, Ponce, and Binner</footer>
</div>
</body>
</html>
