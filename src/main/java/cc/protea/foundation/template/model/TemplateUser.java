package cc.protea.foundation.template.model;

import java.sql.ResultSet;
import java.util.List;

import org.skife.jdbi.v2.Handle;

import com.fasterxml.jackson.annotation.JsonIgnore;

import cc.protea.foundation.integrations.DatabaseUtil;
import cc.protea.platform.ProteaUser;
import cc.protea.platform.UserUtil;

public class TemplateUser extends ProteaUser {

	public String profilePictureUrl;
	
	@JsonIgnore
	public String getUserTableName() {
		return "template_user";
	}

	public static TemplateUser select(Long id) {
		return DatabaseUtil.get(h -> select(h, id));
	}
	public static TemplateUser select(Handle h, Long id) {
		return UserUtil.getProteaUser(h, id);
	}
	
	public static List<TemplateUser> selectAll() {
		return DatabaseUtil.get(h -> selectAll(h));
	}
	public static List<TemplateUser> selectAll(Handle h) {
		return h.createQuery("SELECT * FROM profound_user LEFT JOIN template_user ON template_user.user_key = profound_user.user_key")
			.map(TemplateUser.mapper)
			.list();
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

	@JsonIgnore
	public static Mapper<TemplateUser> mapper = new ProteaUser.Mapper<TemplateUser> () {
		@Override
		public void fill(TemplateUser out, final ResultSet rs) {
			super.fill(out, rs);
			out.profilePictureUrl = DatabaseUtil.getString(rs, "profile_picture_url");
		}
	};

	public void delete() {
		if(this.id == null) {
			return;
		}
		UserUtil.remove(id);
		DatabaseUtil.transaction(h -> {
			h.execute("DELETE FROM " + getUserTableName() + " WHERE user_key = ?", id);
		});
	}

	public void insert(Handle h) {
		// no-op use UserUtil instead
	}
}