<!doctype html>
<html lang="en">
<head>
	<meta charset="utf-8">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<meta name="description" content="URL shortener page">
	<title>Nothing // Shorten URL</title>
	<!-- CDNs -->
	<script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/3.2.1/jquery.min.js" integrity="sha256-hwg4gsxgFZhOsEEamdOYGBf13FyQuiTwlAQgxVSNgt4=" crossorigin="anonymous"></script>
	<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/pure/1.0.0/pure-min.css" integrity="sha256-Q0zCrUs2IfXWYx0uMKJfG93CvF6oVII21waYsAV4/8Q=" crossorigin="anonymous" />
	<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/animate.css/3.5.2/animate.min.css">
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
	<link rel="stylesheet" href="assets/css/layouts/marketing-shorten.css">
	<!--<![endif]-->

	<!-- Locally hosted files -->
	<script src="assets/js/vex.combined.min.js"></script>
	<link rel="stylesheet" href="assets/css/vex.css"/>
	<link rel="stylesheet" href="assets/css/vex-theme-os.css"/>
	<script>vex.defaultOptions.className = 'vex-theme-os'</script>
	<script src="assets/js/piwik.js"></script>
	<script>
		$(function() {
			$.fn.extend({
				animateCss: function (animationName) {
					var animationEnd = 'webkitAnimationEnd mozAnimationEnd MSAnimationEnd oanimationend animationend';
					this.addClass('animated ' + animationName).one(animationEnd, function () {
						$(this).removeClass('animated ' + animationName);
					});
					return this;
				}
			});

			$('input.submit.pure-button').click(function () {
				$.get({
					url: '/api/url/shorten?url=' + $('input.url').val(),
					headers: {"Authorization": "${apikey}"},
					complete: function (r) {
						console.log($('input.url').val());
						console.log(r);
						if (r.status === 200) {
							$('div.shorten-div').attr("style", "display: none;");
							$('div.sh-result').attr("style", "display: block;");
							$('a.sh-link').replaceWith('<a class="sh-link is-center" href="' + 'https://nothing.domains/' + r.responseText + '">' + 'https://nothing.domains/' + r.responseText + '</a>');
							setTimeout(function () {
								$('a.sh-link').animateCss('bounce');
							}, 100);
						} else {
							alert(r.responseText);
						}
					}
				});
			});
		});
	</script>

</head>
<body>

<div class="header">
	<div class="home-menu pure-menu pure-menu-horizontal pure-menu-fixed">
		<a class="pure-menu-heading" href="/">Nothing Domains</a>

		<ul class="pure-menu-list">
			<li class="pure-menu-item">Welcome, ${name}</li>
			<li class="pure-menu-item"><a href="/" class="pure-menu-link">Home</a></li>
			<li class="pure-menu-item"><a href="/tutorial" class="pure-menu-link">Tutorial</a></li>
			<li class="pure-menu-item"><a href="/client-area" class="pure-menu-link">Client Area</a></li>
			<li class="pure-menu-item pure-menu-selected"><a href="/shorten" class="pure-menu-link">Shorten URL</a></li>
			<li class="pure-menu-item"><a href="/premium" class="pure-menu-link">Premium</a></li>
			<li class="pure-menu-item"><a href="/api/login/logout" class="pure-menu-link">Logout</a></li>
		</ul>
	</div>
</div>

<div class="splash-container">
	<div class="splash">
		<h1>URL Shortener</h1>
		<div class="sh-result" style="display:none;">
			<a class="sh-link is-center" target="_blank" href="https://nothing.domains/BIGMASSIVEDOMAIN">https://nothing.domains/BIGMASSIVEDOMAIN</a>
			<p class="disclaim">Remember, you can put any of our domains (including wildcards) in front of the
				"shortened" part, and the link will work.</p>
		</div>
		<div class="pure-form shorten-div">
			<input type="text" name="url" placeholder="URL to Shorten" class="url" required>
			<input type="submit" name="submit" class="submit pure-button" value="Shorten">
		</div>
	</div>

</div>


</body>
</html>
