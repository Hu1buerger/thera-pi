#!/bin/bash

mkdir -p ../dist/latest
cp ../public/latest.template ../dist/latest/latest.html
cat >>../dist/latest/latest.html <<EOF
<p><a href="https://thera-pi.gitlab.io/thera-pi/latest/latest-jars-nolibs.zip">Latest project jar files</a></p>

<table>
<tr><TD>Build-Date</TD><TD>$(date)</TD></tr>
<tr><td>Pipeline-ID</TD><TD>$CI_PIPELINE_ID</TD></TR>
<tr><td>Pipeline-IID</TD><TD>$CI_PIPELINE_IID</TD></TR>
<tr><td>Pipeline-URL</TD><TD><A HREF="$CI_PIPELINE_URL">Pipeline</A></TD></TR>
<tr><td>Job-ID</TD><TD>$CI_JOB_ID</TD></TR>
<tr><td>Job-Name</TD><TD>$CI_JOB_NAME</TD></TR>
</table>
</body>
</html>
EOF
