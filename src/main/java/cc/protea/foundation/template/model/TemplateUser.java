package cc.protea.foundation.template.model;

import java.sql.ResultSet;

import org.skife.jdbi.v2.Handle;

import cc.protea.foundation.integrations.DatabaseUtil;
import cc.protea.foundation.integrations.JsonUtil;
import cc.protea.foundation.utility.ProteaUser;

public class TemplateUser extends ProteaUser {

	public String profilePictureUrl;
	
	public String getTableName() {
		return "template_user";
	}

	@Override
	public UserMapper<TemplateUser> mapper() {
		UserMapper<TemplateUser> mapper = new ProteaUser.UserMapper<TemplateUser>() {
			@Override
			public TemplateUser extend(TemplateUser u, final ResultSet rs) {
				u.profilePictureUrl = DatabaseUtil.getString(rs, "profilePictureUrl");
				return u;
			}
		};
		return mapper;
	}
	
	@Override
	public void update(Handle h, String additionalFields) {
		TemplateUser template = JsonUtil.fromJson(additionalFields, TemplateUser.class);
		if(template == null) {
			super.update(h, additionalFields);
			return;
		} 
		h.createStatement("UPDATE " + getTableName() + " SET "
				+ " profile_picture_url = :profilePictureUrl"
				+ " WHERE user_key = :userKey")
			.bind("profilePictureUrl", template.profilePictureUrl)
			.bind("userKey", this.id)
			.execute();
	}
}
