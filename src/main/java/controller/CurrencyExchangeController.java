package controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import model.HistoricalData;
import model.User;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import service.CurrencyExchangeService;
import service.HistoricalDataService;
import service.SecurityService;
import service.UserService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


/**
 * Handles requests for the application.
 * 
 */
@Controller
@SessionAttributes("userData")
public class CurrencyExchangeController {

	private static final Logger logger = LoggerFactory
			.getLogger(CurrencyExchangeController.class);

	private static final String BASE_CURRENCY = "USD";
	private static final String supportedCurrenciesArr[] = { "AUD",
			"CAD", "GBP", "EUR", "NZD", "JPY" };

	@Autowired
	private CurrencyExchangeService exchangeService;

	@Autowired
	private UserService userService;

	@Autowired
	private HistoricalDataService historicalDataService;

	@Autowired
	private SecurityService securityService;
	
	@RequestMapping(value = "/logout", method = RequestMethod.POST)
	public String loadLogout(Model model, HttpServletRequest request) {

		request.getSession().removeAttribute("userName");
		return "login";
	}

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String landingPage(Model model) {

		return "login";
	}

	/**
	 * Dashboard after successful login
	 * @param userForm
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public String getUserDashboard(@ModelAttribute("userForm") User userForm,
			Model model, HttpServletRequest request) {

		securityService.autologin(userForm.getName(),
				userForm.getPassword());
		HttpSession session = request.getSession();
		
		session.setAttribute("userName", userForm.getName());
		return "redirect:/userDashboard";
	}

	@RequestMapping(value = "/registration", method = RequestMethod.GET)
	public String registerUser(Model model) {

		model.addAttribute("userForm", new User());

		return "registration";
	}

	/**
	 * User registration 
	 * @param userForm
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/registration", method = RequestMethod.POST)
	public String registration(@ModelAttribute("userForm") User userForm,
			Model model, HttpServletRequest request) {
		String password = userForm.getPassword();
		//Register the user
		userService.save(userForm);

		//Auto login after successful register
		securityService.autologin(userForm.getName(), password);

		HttpSession session = request.getSession();
		
		session.setAttribute("userName", userForm.getName());
		return "redirect:/userDashboard";
	}

	/**
	 * Redirect to user dashboard after successful registration
	 * @param userName
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/userDashboard", method = RequestMethod.GET)
	public String registrationuserDashBoard(HttpServletRequest request, Model model) {

		HttpSession session = request.getSession();
		
		String userName = (String) session.getAttribute("userName");
		
		User user = userService.findByUsername(userName);
		
		if(user==null){
			return "login";
		}
		
		// Invoking third party API to get exchange rates.
		JSONObject exchageRate = exchangeService.getExchangeRateFromAPI(supportedCurrenciesArr);

		model.addAttribute("exchangeRate", exchageRate);
		
		//Fetch historical exchange rate data for the user 
		List<HistoricalData> historicalData = historicalDataService
				.findByUsername(userName);
		model.addAttribute("baseCurrency", BASE_CURRENCY);
		List<String> historicalRates = new ArrayList<String>();
		if (historicalData != null) {

			Iterator<HistoricalData> iterator = historicalData.iterator();
			
			while (iterator.hasNext()) {
				HistoricalData histData = iterator.next();
				historicalRates.add(histData.getDate());
				historicalRates.add(histData.getExchangeRates());
			}
		}

		model.addAttribute("historicalRates", historicalRates);
		
		//Make entry of current query into historical records
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    Date now = new Date();
	    String queryTime = sdfDate.format(now);
		historicalDataService.saveHistoricalData(userName, BASE_CURRENCY, exchageRate, queryTime);
		
		
		return "userDashboard";
	}
}
