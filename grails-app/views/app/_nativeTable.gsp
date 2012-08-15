<p>
	<a class="btn"
		onclick="${remoteFunction(action:'getStatusApp', id:appId, update:'nativeTable')}"><i
		class="icon-refresh"></i> Refresh</a>
</p>
<table class="table table-bordered">
	<thead>
		<tr>
			<th>Platform</th>
			<th>Status</th>
			<th>Link</th>
		</tr>
	</thead>
	<tbody>
		<tr>
			<td>Iphone</td>
			<td>
				${statusesMap["iphone"]}
			</td>
			<td><g:if test='${statusesMap["iphone"]=="complete"}'>
					<a href='https://build.phonegap.com/${downLoadMap["iphone"]}'>Click
						To Download</a>
				</g:if> <g:else>
			N/A
			</g:else></td>
		</tr>
		<tr>
			<td>Android</td>
			<td>
				${statusesMap["android"]}
			</td>
			<td><g:if test='${statusesMap["android"]=="complete"}'>
					<a href='https://build.phonegap.com/${downLoadMap["android"]}'>Click
						To Download</a>
				</g:if> <g:else>
			N/A
			</g:else></td>
		</tr>
		<tr>
			<td>Windows Mobile</td>
			<td>
				${statusesMap["winphone"]}
			</td>
			<td><g:if test='${statusesMap["winphone"]=="complete"}'>
					<a href='https://build.phonegap.com/${downLoadMap["winphone"]}'>Click
						To Download</a>
				</g:if> <g:else>
			N/A		</g:else></td>
		</tr>
		<tr>
			<td>Blackberry</td>
			<td>
				${statusesMap["blackberry"]}
			</td>
			<td><g:if test='${statusesMap["blackberry"]=="complete"}'>
					<a href='https://build.phonegap.com/${downLoadMap["blackberry"]}'>Click
						To Download</a>
				</g:if> <g:else>
			N/A		</g:else></td>
		</tr>
		<tr>
			<td>WebOS</td>
			<td>
				${statusesMap["webos"]}
			</td>
			<td><g:if test='${statusesMap["webos"]=="complete"}'>
					<a href='https://build.phonegap.com/${downLoadMap["webos"]}'>Click
						To Download</a>
				</g:if> <g:else>
			N/A		</g:else></td>
		</tr>
		<tr>
			<td>Symbian</td>
			<td>
				${statusesMap["symbian"]}
			</td>
			<td><g:if test='${statusesMap["symbian"]=="complete"}'>
					<a href='https://build.phonegap.com/${downLoadMap["symbian"]}'>Click
						To Download</a>
				</g:if> <g:else>
			N/A		</g:else></td>
		</tr>
	</tbody>
</table>