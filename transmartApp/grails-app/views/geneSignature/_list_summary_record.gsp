<%--
  tranSMART - translational medicine data mart
  
  Copyright 2008-2012 Janssen Research & Development, LLC.
  
  This product includes software developed at Janssen Research & Development, LLC.
  
  This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License 
  as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version,
  along with the following terms:
  1.	You may convey a work based on this program in accordance with section 5, provided that you retain the above notices.
  2.	You may convey verbatim copies of this program code as you receive it, in any medium, provided that you retain the above notices.
  
  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY
  without even the implied warranty of MERCHANTABILITY or FITNESS
  FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public License along with this program.
  If not, see <http://www.gnu.org/licenses/>.
  
 
--%>

<tr class="${(idx % 2) == 0 ? 'odd' : 'even'}">
  <g:set var="dtlLink" value="${createLink(action:'show', id:gs.id)}" />
  <g:set var="ownerFlag" value="${adminFlag || user.id==gs.createdByAuthUser.id}" />
  <g:set var="ctLkup" value="${ctMap.get(gs.id)}" />
	     			
  <td><a onclick="showDialog('GeneSigDetail_${gs.id}', { title: 'Gene Signature Detail [${gs.name?.replaceAll("'","\\\\'").encodeAsHTML()}]', url: '${dtlLink}'})">
  <asset:image alt="detail" style="vertical-align:middle;" src="grid.png" />&nbsp;${gs.name?.replaceAll("'","\\\\'").encodeAsHTML()}</a></td>
  <td>${gs.createdByAuthUser.userRealName?.encodeAsHTML()}</td>
  <td><g:formatDate format="yyyy-MM-dd" date="${gs.dateCreated}" /></td>
  <td style="text-align:center;">${gs.publicFlag ? 'Yes' : 'No'}</td>
  <g:if test="${ctLkup != null}">
    <td style="text-align:center;">${ctLkup?.getAt(1)}</td>
  </g:if>
  <g:else>
    <td style="text-align:center;">0</td>
  </g:else>
  <td><select name="action_${gs.id}" style="font-size: 10px;" onchange="handleActionItem(this, ${gs.id});">
    <option value="">-- Select Action --</option>
    <g:if test="${!gs.deletedFlag && ownerFlag}"><option value="delete">Delete</option></g:if>
    <g:if test="${ownerFlag}"><option value="showEditItems">Edit Items</option></g:if>
    <option value="export">Excel Download</option>
    <option value="gmt">Download .GMT file</option>
    <g:if test="${!gs.publicFlag && ownerFlag}"><option value="public">Make Public</option></g:if>
    <g:if test="${gs.publicFlag && ownerFlag}"><option value="private">Make Private</option></g:if>
  </select>
  </td>
</tr>		     
