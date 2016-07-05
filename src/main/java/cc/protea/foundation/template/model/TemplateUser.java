package cc.protea.foundation.template.model;

import java.sql.ResultSet;

import org.skife.jdbi.v2.Handle;

import cc.protea.foundation.integrations.DatabaseUtil;
import cc.protea.platform.ProteaUser;

public class TemplateUser extends ProteaUser {

	public String profilePictureUrl;
	
	public String getUserTableName() {
		return "template_user";
	}

	@Override
	public void update(Handle h) {
		super.update(h);
		h.createStatement("update template_user set" +
				" user_key = user_key, " +
				" profile_picture_url = :profilePictureUrl " +
				" where user_key = :id")
				.bind("profilePictureUrl", profilePictureUrl)
				.bind("id", id)
				.execute();
	}

	@Override
	public Mapper<TemplateUser> mapper() {
		return mapper;
	}

	public static Mapper<TemplateUser> mapper = new ProteaUser.Mapper<TemplateUser> () {
		@Override
		public void fill(TemplateUser out, final ResultSet rs) {
			super.fill(out, rs);
			out.profilePictureUrl = DatabaseUtil.getString(rs, "profile_picture_url");
		}
	};

//	public void delete(Handle h) {
//		// no-op
//	}
//
//	public void insert(Handle h) {
//		// no-op
//	}
}