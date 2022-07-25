<h2>Heatmap</h2>

<p>

    <div class="plot_hint">
        Click on the heatmap image to open it in a new window as this may increase readability.
        <br><br>
    </div>

    <g:logMsg>Heatmap getContextPath ${request.getContextPath()}</g:logMsg>
    <g:logMsg>Heatmap imageLocations ${imageLocations}</g:logMsg>

    <g:each var='location' in="${imageLocations}">
        <a onclick="window.open('${request.getContextPath()}${location}', '_blank')">
            <img src="${request.getContextPath()}${location}" class='img-result-size'/>
        </a>
    </g:each>

    <g:logMsg>Heatmap ziplink ${ziplink}</g:logMsg>

<g:render template="/plugin/downloadRawDataLink" />

</p>
