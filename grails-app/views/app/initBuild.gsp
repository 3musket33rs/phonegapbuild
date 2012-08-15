
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <title>Grails PhoneGap Build</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="">
    <meta name="author" content="">

    <!-- Le styles -->
    <link href="../css/bootstrap.min.css" rel="stylesheet">
    <style type="text/css">
    body {
        padding-top: 60px;
        padding-bottom: 40px;
    }
    </style>


    <!-- Le HTML5 shim, for IE6-8 support of HTML5 elements -->
    <!--[if lt IE 9]>
      <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
    <![endif]-->


</head>

<body>

<div class="navbar navbar-fixed-top">
    <div class="navbar-inner">
        <div class="container">
            <a class="btn btn-navbar" data-toggle="collapse"
               data-target=".nav-collapse"> <span class="icon-bar"></span> <span
                    class="icon-bar"></span> <span class="icon-bar"></span>
            </a> <a class="brand" href="#">Grails PhoneGap Build Plugin</a>

        </div>
    </div>
</div>

<div class="container">

    <div class="hero-unit" style="text-align: center;">
        <img alt="" src="../images/phonegap-build.png" height="125" width="125" />
    </div>

    <!-- Example row of columns -->
    <div class="row">
        <div class="span6">
            <p>
                <a class="btn btn-primary btn-large"
                   onclick="${remoteFunction(action:'push', update:'nativeTable')}">Push
                ${appName} to the build factory
                </a>
            </p>
        </div>
        <div class="span6">

            <p>
            <div id="nativeTable"></div>
        </p>
        </div>

    </div>

    <hr>


</div>
<div id="spinner" class="spinner" style="display: none;">
    <img src="../images/spinner.gif" alt="Loading..." width="50"
         height="50" />
</div>
<!-- /container -->

<!-- Le javascript
    ================================================== -->
<!-- Placed at the end of the document so the pages load faster -->
<script src="http://code.jquery.com/jquery-1.7.1.min.js"></script>
<script src="../js/bootstrap.min.js"></script>
<script type="text/javascript">
    $(document).ready(function() {
                $("#spinner").bind("ajaxSend", function() {
                    $(this).fadeIn();
                }).bind("ajaxComplete", function() {
                            $(this).fadeOut();
                        })}
    );
</script>
</body>
</html>
