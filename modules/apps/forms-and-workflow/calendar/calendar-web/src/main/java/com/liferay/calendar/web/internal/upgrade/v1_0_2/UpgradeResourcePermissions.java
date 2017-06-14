/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.calendar.web.internal.upgrade.v1_0_2;

import com.liferay.calendar.model.CalendarResource;
import com.liferay.calendar.service.CalendarResourceLocalService;
import com.liferay.portal.kernel.model.ResourceBlock;
import com.liferay.portal.kernel.model.ResourceBlockPermissionsContainer;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.RoleConstants;
import com.liferay.portal.kernel.service.ResourceBlockLocalService;
import com.liferay.portal.kernel.service.ResourceBlockPermissionLocalService;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.util.Validator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author José María Muñoz
 */
public class UpgradeResourcePermissions extends UpgradeProcess {

	public UpgradeResourcePermissions(
		ResourceBlockLocalService resourceBlockLocalService,
		CalendarResourceLocalService calendarResourceLocalService,
		ResourceBlockPermissionLocalService
			resourceBlockPermissionLocalService,
		RoleLocalService roleLocalService) {

		_resourceBlockLocalService = resourceBlockLocalService;
		_calendarResourceLocalService = calendarResourceLocalService;
		_resourceBlockPermissionLocalService =
			resourceBlockPermissionLocalService;
		_roleLocalService = roleLocalService;
	}

	public void upgradeResourcePermissions() throws Exception {
		int contCalendarResource =
			_calendarResourceLocalService.getCalendarResourcesCount();

		List<CalendarResource> calendarResources =
			_calendarResourceLocalService.getCalendarResources(
				0, contCalendarResource);

		for (CalendarResource calendarResource : calendarResources) {
			ResourceBlock resourceBlock =
				_resourceBlockLocalService.getResourceBlock(
					calendarResource.getResourceBlockId());

			ResourceBlockPermissionsContainer
				resourceBlockPermissionsContainer =
					_resourceBlockPermissionLocalService.
						getResourceBlockPermissionsContainer(
							calendarResource.getResourceBlockId());

			Map<Long, String[]> roleIdsToActionIds = new HashMap<>();

			Set<Long> roleIds = resourceBlockPermissionsContainer.getRoleIds();

			boolean existUserGuest = false;

			for (long roleId : roleIds) {
				List<String> actionIds = new ArrayList<>();
				Role role = _roleLocalService.getRole(roleId);

				if (Validator.isNotNull(role) &&
					!RoleConstants.GUEST.equals(role.getName())) {

					actionIds = _resourceBlockLocalService.getPermissions(
						resourceBlock, roleId);
				}
				else {
					existUserGuest = true;
				}

				roleIdsToActionIds.put(
					roleId, actionIds.toArray(new String[actionIds.size()]));
			}

			if (existUserGuest) {
				_resourceBlockLocalService.setIndividualScopePermissions(
					calendarResource.getCompanyId(),
					calendarResource.getGroupId(), _CALENDAR_RESOURCE_NAME,
					calendarResource.getPrimaryKey(), roleIdsToActionIds);
			}
		}
	}

	@Override
	protected void doUpgrade() throws Exception {
		upgradeResourcePermissions();
	}

	private static final String _CALENDAR_RESOURCE_NAME =
		"com.liferay.calendar.model.CalendarResource";

	private final CalendarResourceLocalService _calendarResourceLocalService;
	private final ResourceBlockLocalService _resourceBlockLocalService;
	private final ResourceBlockPermissionLocalService
		_resourceBlockPermissionLocalService;
	private final RoleLocalService _roleLocalService;

}