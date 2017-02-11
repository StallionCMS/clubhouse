package io.stallion.clubhouse;

import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;

import io.stallion.restfulEndpoints.BodyParam;
import io.stallion.restfulEndpoints.EndpointResource;
import io.stallion.services.Log;
import io.stallion.users.IUser;
import io.stallion.users.UserController;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Produces("application/json")
@Path("/clubhouse-api/auth")
public class AuthEndpoints implements EndpointResource {
    @POST
    @Path("/login")
    public Object login(@BodyParam("username") String username, @BodyParam("password") String password) {
        IUser user = UserController.instance().loginUser(username, password, false);
        UserProfile up = UserProfileController.instance().forStallionUserOrNotFound(user.getId());
        return map(
                val(
                        "user", user
                ),
                val("defaultChannelId", 10500L),
                val("userProfile", up)
        );
    }
}
