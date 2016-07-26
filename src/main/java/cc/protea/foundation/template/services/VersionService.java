package cc.protea.foundation.template.services;

import java.util.Date;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

import cc.protea.foundation.integrations.HerokuUtil;

@Api(value = "/version", description = "Release version info")
@Path("/version")
@Produces(MediaType.APPLICATION_JSON)
public class VersionService {
	
	@GET
	@ApiOperation(value = "Version stamp")
	@Produces(MediaType.TEXT_PLAIN)
	public String version() {
		return HerokuUtil.getReleaseVersion() + " " + HerokuUtil.getSlugCommit();
	}
	
	@GET
	@Path("/details")
	@ApiOperation(value = "Release details")
	public ReleaseDetails details() {
		ReleaseDetails details = new ReleaseDetails();
		details.appName = HerokuUtil.getAppName();
		details.slugCommit = HerokuUtil.getSlugCommit();
		details.slugDescription = HerokuUtil.getSlugDescription();
		details.releaseVersion = HerokuUtil.getReleaseVersion();
		details.releaseCreatedAt = HerokuUtil.getReleaseCreatedAt();
		return details;
	}
	
	public static class ReleaseDetails {
		public String appName;
		public String slugCommit;
		public String slugDescription;
		public String releaseVersion;
		public Date releaseCreatedAt;
	}

}
