package com.customer.servlets;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONObject;

import com.customer.DAOImpl.CustomerDAOImpl;
import com.customer.Model.Customer;
import com.customer.Utility.CustomerList;

@WebServlet("/CustomerDetails")
public class CustomerDetails extends HttpServlet {

	private static final long serialVersionUID = 1L;
	static private CustomerDAOImpl cimpl;
	static private HttpSession session;
	static private List<Customer> getAllCustomer;
	static private CustomerList clist ;

	@Override
	public void init() throws ServletException {
		cimpl = new CustomerDAOImpl();
		clist= new CustomerList();

	}

	

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		session = req.getSession();
		getAllCustomer = (List<Customer>) session.getAttribute("AllCustomers");

		if (getAllCustomer == null || req.getParameter("action").equals("viewall")) {
			getAllCustomer = cimpl.getAllCustomer();
			session.setAttribute("AllCustomers", getAllCustomer);
			req.getRequestDispatcher("Customerdetails.jsp").forward(req, resp);
			return;
		}

		if (session.getAttribute("Token") != null) {
			String action = req.getParameter("action");
			if (action != null) {
				if (action.equals("search")) {
					searchCustomer(req);
				} else if (action.equals("delete")) {
					deleteCustomer(req);
				} else if (action.equals("edit")) {
					editCoustomer(req, resp);
				} else if (action.equals("update")) {
					updateCustomer(req);
				} else if (action.equals("Add")) {
					addCustomer(req);
				} else if (action.equals("Pageing")) {
					paging(req);
				} else if (action.equals("Sync")) {
					syncing(req);
				}
			}

		} else {

			req.getRequestDispatcher("LogIn.jsp").forward(req, resp);
			return;
		}

		session.setAttribute("AllCustomers", getAllCustomer);
		req.getRequestDispatcher("Customerdetails.jsp").include(req, resp);

	}

	private void searchCustomer(HttpServletRequest req) {

		String SearchBy = req.getParameter("SearchBy");
		String searchInput = req.getParameter("searchInput").trim();

		if (SearchBy.equals("firstname")) {
			getAllCustomer = cimpl.getCustomerByFirst_name(searchInput);
		} else if (SearchBy.equals("city")) {
			getAllCustomer = cimpl.getCustomerByCity(searchInput);
		} else if (SearchBy.equals("email")) {
			getAllCustomer = cimpl.getCustomerByEmail(searchInput);
		} else if (SearchBy.equals("phone")) {
			long longsearch = Long.parseLong(searchInput);
			getAllCustomer = cimpl.getCustomerByPhone_number(longsearch);
		}
		if (getAllCustomer.isEmpty()) {

			req.setAttribute("Error", "Invalid Input.Customer Not Found.");
		}

	}

	private void deleteCustomer(HttpServletRequest req) {

		String CustomerId = req.getParameter("cid");
		Customer customerById = cimpl.getCustomerById(CustomerId);
		cimpl.deleteCustomer(CustomerId);
		getAllCustomer = cimpl.getAllCustomer();
		req.setAttribute("message", "Customer "+customerById.getFirst_name()+" "+customerById.getLast_name()+" Deleted Sucessfully. ");

	}

	private void editCoustomer(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String CustomerId = req.getParameter("cid");
		Customer customerById = cimpl.getCustomerById(CustomerId);

		req.setAttribute("EditCoustomer", customerById);
		req.getRequestDispatcher("EditCustomer.jsp").forward(req, resp);

	}

	private void updateCustomer(HttpServletRequest req) {
		String firstname = req.getParameter("firstname");
		String lastname = req.getParameter("lastname");
		String email = req.getParameter("email");
		String phonenumber = req.getParameter("phonenumber").trim();
		long longphone = Long.parseLong(phonenumber);
		String street = req.getParameter("street");
		String Address = req.getParameter("Address");
		String city = req.getParameter("city");
		String state = req.getParameter("state");
		String customerId = req.getParameter("cid");

		Customer c = new Customer(customerId, firstname, lastname, street, Address, city, state, email, longphone);
		cimpl.updateCustomer(c);
		getAllCustomer = cimpl.getAllCustomer();
		session.setAttribute("AllCustomers", getAllCustomer);
		req.setAttribute("message", "Customer "+firstname+" "+lastname+" Successfully updated. ");

	}

	private void addCustomer(HttpServletRequest req) {
		String firstname = req.getParameter("firstname");
		String lastname = req.getParameter("lastname");
		String email = req.getParameter("email");
		String phonenumber = req.getParameter("phonenumber").trim();
		long longphone = Long.parseLong(phonenumber);
		String street = req.getParameter("street");
		String Address = req.getParameter("Address");
		String city = req.getParameter("city");
		String state = req.getParameter("state");

		Customer c = new Customer(firstname, lastname, street, Address, city, state, email, longphone);
		cimpl.addCustomer(c);
		getAllCustomer = cimpl.getAllCustomer();
		session.setAttribute("AllCustomers", getAllCustomer);
		req.setAttribute("message", "Customer "+firstname+" "+lastname+" Successfully Added. ");

	}

	private void paging(HttpServletRequest req) {
//		System.out.println(req.getParameter("pageNumber"));
		 String pageNumber = req.getParameter("pageNumber");
		 if(Integer.parseInt(pageNumber)>0) {
			 req.setAttribute("pageNumber",pageNumber);
		 }else {
			 System.out.println("else");
			 req.setAttribute("pageNumber",null);
		 }
		
	}

	private void syncing(HttpServletRequest req) {
		
		HttpSession session = req.getSession();
		String token = (String) session.getAttribute("Token");
		
		 JSONArray listArray = clist.getCustomerListArray(token);
		if (listArray != null) {
			for (int i = 0; i < listArray.length(); i++) {
				JSONObject jsonObject = listArray.getJSONObject(i);
				
				
				String uuid = jsonObject.getString("uuid");
				String first_name = jsonObject.getString("first_name");
				String last_name = jsonObject.getString("last_name");
				String street = jsonObject.getString("street");
				String address = jsonObject.getString("address");
				String city = jsonObject.getString("city");
				String state = jsonObject.getString("state");
				String email = jsonObject.getString("email");
				Long phone ;
				try {
					
					 phone = Long.parseLong(jsonObject.getString("phone"));
				}catch (Exception e) {
					phone = 0l;
				}
				
				

				Customer c = new Customer(uuid, first_name, last_name, street, address, city, state, email,phone);
				cimpl.addCustomer(c);

			}

		}

		getAllCustomer = cimpl.getAllCustomer();
		session.setAttribute("AllCustomers", getAllCustomer);
		req.setAttribute("message", "Customers Synced Successfully. ");
		
	}

}
