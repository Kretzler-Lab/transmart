<asset:javascript src="jquery-plugin.js"/>

<td>
    <g:select class='addremoveselect' name='userstoremove'
              from="${ug?.members.sort { it.name.toUpperCase() }}"
              size='15' multiple='yes' optionKey='id'/>
</td>
<td class="addremovebuttonholder">
    <button class="ltarrowbutton"
            onclick="${remoteFunction(action:'addUsersToUserGroup',update:[success:'groupmembers', failure:''], id:ug?.id, params:'jQuery(\'#userstoadd\').serialize()+\'&searchtext=\'+document.getElementById(\'searchtext\').value'  )};
		     return false;">&LT;&LT;Add</button><br>
	<button class="ltarrowbutton"
		onclick="${remoteFunction(action:'removeUsersFromUserGroup',update:[success:'groupmembers', failure:''], id:ug?.id, params:'jQuery(\'#userstoremove\').serialize()+\'&searchtext=\'+document.getElementById(\'searchtext\').value'  )};
			 return false;">Remove&GT;&GT;</button>
</td>
<td>
    <div id="addfrombox">
        <g:select class='addremoveselect' width='100px' size='15' name='userstoadd'
                  from="${usersToAdd}" multiple='yes' optionKey='id'/>
</td>
</div>
</td>
