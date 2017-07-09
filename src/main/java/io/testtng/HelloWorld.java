package io.testtng;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloWorld 
{
	@RequestMapping("/hello")
	public String sayHello()
	{
		return "hi every one from unibz";
	}

}
