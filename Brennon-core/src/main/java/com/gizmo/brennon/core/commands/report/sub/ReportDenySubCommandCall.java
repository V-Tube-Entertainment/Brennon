package com.gizmo.brennon.core.commands.report.sub;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.command.CommandCall;
import com.gizmo.brennon.core.api.job.jobs.UserLanguageMessageJob;
import com.gizmo.brennon.core.api.storage.dao.Dao;
import com.gizmo.brennon.core.api.storage.dao.OfflineMessageDao.OfflineMessage;
import com.gizmo.brennon.core.api.storage.dao.ReportsDao;
import com.gizmo.brennon.core.api.user.interfaces.User;
import com.gizmo.brennon.core.api.utils.MathUtils;
import com.gizmo.brennon.core.api.utils.placeholders.MessagePlaceholders;

import java.util.List;
import java.util.Optional;

public class ReportDenySubCommandCall implements CommandCall
{

    @Override
    public void onExecute( final User user, final List<String> args, final List<String> parameters )
    {
        if ( args.size() != 1 )
        {
            user.sendLangMessage( "general-commands.report.deny.usage" );
            return;
        }

        if ( !MathUtils.isLong( args.get( 0 ) ) )
        {
            user.sendLangMessage( "no-number" );
            return;
        }

        final long id = Long.parseLong( args.get( 0 ) );
        final Dao dao = BuX.getApi().getStorageManager().getDao();
        final ReportsDao reportsDao = dao.getReportsDao();

        reportsDao.getReport( id ).thenAccept( report ->
        {
            if ( report == null )
            {
                user.sendLangMessage( "general-commands.report.deny.not-found" );
                return;
            }

            reportsDao.handleReport( id, false );
            user.sendLangMessage(
                "general-commands.report.deny.updated",
                report
            );

            Optional<User> optionalUser = BuX.getApi().getUser( report.getReportedBy() );
            MessagePlaceholders placeholders = MessagePlaceholders.create()
                .append( report )
                .append( "staff", user.getName() );

            if ( optionalUser.isPresent() )
            {
                final User target = optionalUser.get();

                target.sendLangMessage(
                    "general-commands.report.deny.denied",
                    placeholders
                );
            }
            else if ( BuX.getApi().getPlayerUtils().isOnline( report.getReportedBy() ) )
            {
                BuX.getInstance().getJobManager().executeJob( new UserLanguageMessageJob(
                    report.getReportedBy(),
                    "general-commands.report.deny.denied",
                    placeholders
                ) );
            }
            else
            {
                BuX.getApi().getStorageManager().getDao().getOfflineMessageDao().sendOfflineMessage(
                    report.getReportedBy(),
                    new OfflineMessage(
                        null,
                        "general-commands.report.deny.denied",
                        placeholders
                    )
                );
            }
        } );
    }

    @Override
    public String getDescription()
    {
        return "Denies a report with a given id.";
    }

    @Override
    public String getUsage()
    {
        return "/report deny (id)";
    }
}
