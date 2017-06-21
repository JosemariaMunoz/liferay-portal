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
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.RoleConstants;
import com.liferay.portal.kernel.service.ResourceBlockLocalService;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;

import java.util.List;

/**
 * @author José María Muñoz
 */
public class UpgradeResourcePermissions extends UpgradeProcess {

	public UpgradeResourcePermissions(
		CalendarResourceLocalService calendarResourceLocalService,
		ResourceBlockLocalService resourceBlockLocalService,
		RoleLocalService roleLocalService) {

		_calendarResourceLocalService = calendarResourceLocalService;
		_resourceBlockLocalService = resourceBlockLocalService;
		_roleLocalService = roleLocalService;
	}

	public void upgradeResourcePermissions() throws Exception {
		int contCalendarResource =
			_calendarResourceLocalService.getCalendarResourcesCount();

		List<CalendarResource> calendarResources =
			_calendarResourceLocalService.getCalendarResources(
				0, contCalendarResource);

		for (CalendarResource calendarResource : calendarResources) {
			Role guestRole = _roleLocalService.getRole(
				calendarResource.getCompanyId(), RoleConstants.GUEST);

			_resourceBlockLocalService.removeIndividualScopePermissions(
				calendarResource.getCompanyId(), calendarResource.getGroupId(),
				_CALENDAR_RESOURCE_NAME, calendarResource.getPrimaryKey(),
				guestRole.getRoleId(), 0);
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
	private final RoleLocalService _roleLocalService;

}