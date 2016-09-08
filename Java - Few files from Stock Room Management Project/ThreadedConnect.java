
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;
import javax.swing.JOptionPane;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Mohit Mittal SID: 4677985
 */
public class ThreadedConnect implements Runnable {

    private Socket s;
    String completedStatus = null;
    String dateOrderCreated;
    String dateExpectedDelivery;
    private PreparedStatement statement;
    private ResultSet rset;
    private ObjectInputStream is = null;  // Streams definition for ThreadedConnect
    private ObjectOutputStream os = null;
    String sMakeSelect;
    Parts getPartsDataObject = null;
    Orders getOrdersDataObject = null;
    String sDriverName = "org.sqlite.JDBC";
    int iTimeout = 30;
    Integer defaultPartID = 0;
    String getLastPartID;
    Integer currentPartID;
    Orders singleOrder;
    Integer currentOrderID;
    private Integer getPartIDFromDB;

    public ThreadedConnect(Socket s) {
        this.s = s;
    }
    Connection conn = null;

    ThreadedConnect() {
    }

    @Override
    public void run() {
        try {
            this.is = new ObjectInputStream(this.s.getInputStream());
            this.os = new ObjectOutputStream(this.s.getOutputStream());

            conn = dbConnect();

            while (true) {

                try {
                   // Receive the operation from the client
                    // Receive the object
                    Message receivedMessage = (Message) this.is.readObject();
                    switch (receivedMessage.getMessage()) {
                        case "STORE_PART":
                            System.out.println("Store Part Enter");
                            getPartsDataObject = (Parts) receivedMessage.getData();

                            try {
                                addPart(conn, getPartsDataObject, getPartIDFromDB);
                            } catch (Exception e) {
                                acknowledge("Part Not Saved", null);
                            }

                            acknowledge("Part Saved", null);
                            break;
                        case "Store_Order":
                            System.out.println("Store Order Enter");
                            getOrdersDataObject = (Orders) receivedMessage.getData();
                            try {
                                addOrder(conn, getOrdersDataObject, getOrderID(conn));
                            } catch (Exception e) {
                                acknowledge("Order Not Saved", null);
                            }
                            acknowledge("Order Saved", null);
                            break;
                        case "Part_Data_From_DB":

                            ArrayList<Parts> partData = null;
                            try {
                                partData = getEverythingfromPart(conn);
                            } catch (Exception e) {
                                acknowledge("Can't get everything from Parts table", null);
                            }

                            acknowledge("Sending everything from Parts table", partData);
                            break;
                        case "Everything_from_OrderDB":

                            ArrayList<Orders> allOrders = null;
                            try {
                                allOrders = getAllOrders(conn);
                            } catch (Exception e) {
                                acknowledge("Can't get All Orders", null);
                            }

                            acknowledge("Send Orders Detail", allOrders);
                            break;
                        case "getSelectedOrder":
                            Integer orID = (int) receivedMessage.getData();

                            try {
                                singleOrder = getOrder(conn, orID);
                            } catch (Exception e) {
                                acknowledge("Can't get selected Order", null);
                            }

                            acknowledge("Send single Order Detail", singleOrder);
                            break;
                        case "Modify_Part":
                            System.out.println("Modify part enter");
                            Parts getPartsObject = (Parts) receivedMessage.getData();
                            try {
                                modifyPart(conn, getPartsObject);
                            } catch (Exception e) {
                                acknowledge("Part not modified", null);
                            }
                            acknowledge("Part Modified", null);
                            break;
                        case "Modify_Order":
                            System.out.println("Modify order enter");
                            Orders getModifiedObject = (Orders) receivedMessage.getData();
                            try {
                                modifyOrder(conn, getModifiedObject);
                            } catch (Exception e) {
                                acknowledge("Order not modified", null);
                            }
                            acknowledge("Order Modified", null);
                            break;
                        case "Delete_Part":
                            Integer partID = (int) receivedMessage.getData();
                            try {
                                removePart(conn, partID);
                            } catch (Exception e) {
                                acknowledge("Part not deleted", null);
                            }
                            acknowledge("Part deleted", null);
                            break;
                        case "Delete_Order":
                            Integer orderID = (int) receivedMessage.getData();
                            try {
                                removeOrder(conn, orderID);
                            } catch (Exception e) {
                                acknowledge("Order Can't be deleted", null);
                            }
                            acknowledge("Order Deleted", null);
                            break;
                        default:
                            break;

                    }

                    // Check the operation and do accordingly.
                } catch (IOException ex) {
                    Logger.getLogger(ThreadedConnect.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(ThreadedConnect.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        } catch (IOException ex) {
            Logger.getLogger(ThreadedConnect.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private Connection dbConnect() {
        try {
            Class.forName(sDriverName);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ThreadedConnect.class.getName()).log(Level.SEVERE, null, ex);
        }

        // now we set up a set of fairly basic string variables to use in the body of the code proper
        String sTempDb = "308SEdb.s3db";
        String sJdbc = "jdbc:sqlite";
        String sDbUrl = sJdbc + ":" + sTempDb;
        try {
            conn = DriverManager.getConnection(sDbUrl);
        } catch (SQLException ex) {
            Logger.getLogger(ThreadedConnect.class.getName()).log(Level.SEVERE, null, ex);
        }
        return conn;
    }

    private void addPart(Connection conn, Parts obj, Integer getPartIDFromDB) throws SQLException {
        String sMakeSelect = "SELECT * from Parts";

        String insert = "insert into Parts values(" + getPartIDFromDB + ", '" + obj.getManufacturer() + "', '" + obj.getManuf_part_ID() + "', '" + obj.getDescription() + "', " + obj.getStock_level() + ", " + obj.getLow_stock_limit() + ", '" + obj.getShelf_location() + "', " + obj.getPrice() + " )";

        Statement stmt = conn.createStatement();
        stmt.setQueryTimeout(iTimeout);
        stmt.executeUpdate(insert);

        ResultSet getEverythingFromParts = stmt.executeQuery(sMakeSelect);
        while (getEverythingFromParts.next()) {
            String manuf = getEverythingFromParts.getString("Manufacturer");
        }
    }

    private void modifyPart(Connection conn, Parts obj) throws SQLException {
        System.out.println("Modify Part enter");
        String insert = "Update Parts set Manufacturer='" + obj.getManufacturer() + "', Manufacturer_part_number='" + obj.getManuf_part_ID() + "', Description='" + obj.getDescription() + "', Current_stock_level=" + obj.getStock_level() + ", Low_stock_level=" + obj.getLow_stock_limit() + ", Shelf_location='" + obj.getShelf_location() + "', Price=" + obj.getPrice() + " Where Part_id=" + obj.getPartID();
        Statement stmt = conn.createStatement();
        stmt.setQueryTimeout(iTimeout);
        stmt.executeUpdate(insert);
    }

    private void modifyOrder(Connection connection, Orders order) throws SQLException {
        System.out.println("Modify Order enter");
        deletePartsLinkWithOrder(order.getOrder_id(), conn);
        int i = 1;
        statement = connection.prepareStatement("UPDATE Orders SET Date_created=?, Supplier=?, "
                + "Status=?, Expected_delivery=?, Completed_status=? "
                + "WHERE Order_id=?;");
        statement.setString(i++, order.getDate_created());
        statement.setString(i++, order.getSupplier());
        statement.setString(i++, order.getStatus());
        statement.setString(i++, order.getExpected_delivery());
        statement.setBoolean(i++, order.getCompleted_status());
        statement.setInt(i++, order.getOrder_id());
        statement.executeUpdate();
        statement.close();
        for (quantifiedParts part : order.getlParts()) {
            storePartLinkedToOrder(order.getOrder_id(), part, conn);
        }

    }

    private void removePart(Connection conn, Integer pid) throws SQLException {
        System.out.println("Delete Part");

        String psString = "Delete from Parts where Part_id = " + pid;

        Statement stmt = conn.createStatement();
        stmt.setQueryTimeout(iTimeout);
        stmt.executeUpdate(psString);
        System.out.println("Part Deleted");
    }

    private void removeOrder(Connection conn, Integer oid) throws SQLException {

        deletePartsLinkWithOrder(oid, conn);
        System.out.println("Delete Order Enter");

        String psString = "Delete from Orders where Order_id = " + oid;

        Statement stmt = conn.createStatement();
        stmt.setQueryTimeout(iTimeout);
        stmt.executeUpdate(psString);
        System.out.println("Deleted");
    }

    private void deletePartsLinkWithOrder(Integer orderId, Connection conn) throws SQLException {
        statement = conn.prepareStatement("DELETE FROM Purchase_order_line WHERE Order_id=?");
        statement.setInt(1, orderId);
        statement.executeUpdate();
        statement.close();
    }

    private ArrayList<Parts> getEverythingfromPart(Connection conn) throws SQLException {
        ArrayList<Parts> results = new ArrayList<Parts>();
        PreparedStatement statement = conn.prepareStatement("SELECT * FROM Parts");
        ResultSet rset = statement.executeQuery();
        while (rset.next()) {
            Parts part = new Parts();
            part.setPartID(rset.getInt("Part_id"));
            part.setManufacturer(rset.getString("Manufacturer"));
            part.setManuf_part_ID(rset.getString("Manufacturer_part_number"));
            part.setDescription(rset.getString("Description"));
            part.setStock_level(rset.getInt("Current_stock_level"));
            part.setLow_stock_limit(rset.getInt("Low_stock_level"));
            part.setShelf_location(rset.getString("Shelf_location"));
            part.setPrice(rset.getFloat("Price"));
            results.add(part);
        }
        statement.close();
        return results;
    }

    public Integer getGetPartIDFromDB() {
        return getPartIDFromDB;
    }

    private Integer getPartID(Connection conn) {
        try {
            Statement stmt = conn.createStatement();
            String queryLastPartID = "SELECT * FROM Parts ORDER BY Part_id DESC LIMIT 1";
            stmt.setQueryTimeout(iTimeout);
            ResultSet getLastPartRecord = stmt.executeQuery(queryLastPartID);

            while (getLastPartRecord.next()) {
                currentPartID = Integer.parseInt(getLastPartRecord.getString("Part_id"));
                currentPartID++;
            }
        } catch (SQLException ex) {
            Logger.getLogger(ThreadedConnect.class.getName()).log(Level.SEVERE, null, ex);
        }
        return currentPartID;

    }

    private Integer getOrderID(Connection conn) {
        try {
            Statement stmt = conn.createStatement();
            String queryLastPartID = "SELECT * FROM Orders ORDER BY Order_id DESC LIMIT 1";
            stmt.setQueryTimeout(iTimeout);
            ResultSet getLastPartRecord = stmt.executeQuery(queryLastPartID);

            while (getLastPartRecord.next()) {
                currentOrderID = Integer.parseInt(getLastPartRecord.getString("Order_id"));
                currentOrderID++;
            }
        } catch (SQLException ex) {
            Logger.getLogger(ThreadedConnect.class.getName()).log(Level.SEVERE, null, ex);
        }
        return currentOrderID;

    }

    private void acknowledge(String command, Object data) {
        try {
            Message msg = new Message(command, data);
            this.os.writeObject(msg);
            this.os.flush();
        } catch (IOException ex) {
            Logger.getLogger(ThreadedConnect.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void addOrder(Connection conn, Orders ordersDataObject, Integer OrderIDFromDB) throws SQLException, ParseException {
        System.out.println("addOrder Enter");
        Integer cStatus = (ordersDataObject.getCompleted_status()) ? 1 : 0;

        String insert = "Insert into Orders values(" + OrderIDFromDB + ", '" + ordersDataObject.getDate_created() + "', '" + ordersDataObject.getSupplier() + "', '" + ordersDataObject.getStatus() + "', '" + ordersDataObject.getExpected_delivery() + "', '" + cStatus + "');";
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(insert);

        for (quantifiedParts part : ordersDataObject.getlParts()) {
            storePartLinkedToOrder(OrderIDFromDB, part, conn);
        }
    }

    private void storePartLinkedToOrder(int order_id, quantifiedParts part, Connection conn) throws SQLException {
        int i = 1;
        System.out.println("storePartLinkedToOrder Enter");
        statement = conn.prepareStatement("INSERT INTO Purchase_order_line VALUES(NULL,?,?,?);");
        statement.setInt(i++, order_id);
        statement.setInt(i++, part.getPartID());
        statement.setInt(i++, part.getQty());
        statement.executeUpdate();
        statement.close();
    }

    private Orders getOrder(Connection conn, Integer OrderIDFromDB) throws SQLException {
        Orders order = new Orders();

        statement = conn.prepareStatement("SELECT * FROM Orders WHERE Order_id=?;");
        statement.setInt(1, OrderIDFromDB);
        rset = statement.executeQuery();
        while (rset.next()) {
            order.setOrder_id(rset.getInt("Order_id"));
            order.setDate_created(rset.getString("Date_created"));
            order.setSupplier(rset.getString("Supplier"));
            switch (rset.getString("Status")) {
                case "Ordered":
                    order.setStatus("Ordered");
                    break;
                case "On-Hold":
                    order.setStatus("On-Hold");
                    break;
                case "Cancelled":
                    order.setStatus("Cancelled");
                    break;
                case "Dispatched":
                    order.setStatus("Dispatched");
                    break;
                case "Completed":
                    order.setStatus("Completed");
                    break;
                default:
                    break;
            }
            order.setExpected_delivery(rset.getString("Expected_delivery"));
            order.setCompleted_status(rset.getBoolean("Completed_status"));
        }
        statement.close();
        ArrayList<quantifiedParts> parts = null;
        parts = getPartsLinkedToOrder(OrderIDFromDB);
        order.setlParts(parts);
        return order;

    }

    private ArrayList<quantifiedParts> getPartsLinkedToOrder(Integer OrderIDFromDB) throws SQLException {
        ArrayList<quantifiedParts> parts = new ArrayList<quantifiedParts>();
        statement = conn.prepareStatement("SELECT part.Part_id, part.Manufacturer, part.Manufacturer_part_number, "
                + "part.Description, part.Current_stock_level, "
                + "part.Low_stock_level, part.Shelf_location, part.Price, Orderline.Quantity "
                + "FROM Parts part, Purchase_order_line Orderline "
                + "WHERE part.Part_id=Orderline.Part_Id AND Orderline.Order_id=?");
        statement.setInt(1, OrderIDFromDB);
        rset = statement.executeQuery();
        while (rset.next()) {
            quantifiedParts part = new quantifiedParts();
            part.setPartID(rset.getInt("Part_id"));
            part.setManufacturer(rset.getString("Manufacturer"));
            part.setManuf_part_ID(rset.getString("Manufacturer_part_number"));
            part.setDescription(rset.getString("Description"));
            part.setStock_level(rset.getInt("Current_stock_level"));
            part.setLow_stock_limit(rset.getInt("Low_stock_level"));
            part.setShelf_location(rset.getString("Shelf_location"));
            part.setPrice(rset.getFloat("Price"));
            part.setQty(rset.getInt("Quantity"));
            parts.add(part);
        }
        statement.close();
        return parts;
    }

    private ArrayList<Orders> getAllOrders(Connection conn) throws SQLException {
        ArrayList<Orders> results = new ArrayList<Orders>();
        PreparedStatement pStatement = conn.prepareStatement("SELECT * FROM Orders");
        ResultSet rset = pStatement.executeQuery();
        while (rset.next()) {
            Orders order = new Orders();
            order.setOrder_id(rset.getInt("Order_id"));
            order.setDate_created(rset.getString("Date_created"));
            order.setSupplier(rset.getString("Supplier"));
            switch (rset.getString("Status")) {
                case "Ordered":
                    order.setStatus("Ordered");
                    break;
                case "On-Hold":
                    order.setStatus("On-Hold");
                    break;
                case "Cancelled":
                    order.setStatus("Cancelled");
                    break;
                case "Dispatched":
                    order.setStatus("Dispatched");
                    break;
                case "Completed":
                    order.setStatus("Completed");
                    break;
                default:
                    break;
            }
            order.setExpected_delivery(rset.getString("Expected_delivery"));
            order.setCompleted_status(rset.getBoolean("Completed_status"));
            results.add(order);
        }

        pStatement.close();
        for (Orders order : results) {
            ArrayList<quantifiedParts> parts = null;
            parts = getPartsLinkedToOrder(order.getOrder_id());
            order.setlParts(parts);
        }
        return results;
    }

}
