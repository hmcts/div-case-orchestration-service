package uk.gov.hmcts.reform.divorce.orchestration.repositories.mapping;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import uk.gov.hmcts.reform.divorce.orchestration.models.UserRole;


import java.sql.ResultSet;
import java.sql.SQLException;

public class UserRolesMapper implements ResultSetMapper<UserRole> {
    @Override
    public UserRole map(int index, ResultSet result, StatementContext ctx) throws SQLException {

        return new UserRole(
            result.getString("user_id"),
            result.getString("role")
        );
    }
}
