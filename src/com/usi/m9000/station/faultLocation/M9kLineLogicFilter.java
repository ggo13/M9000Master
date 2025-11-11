package com.usi.m9000.station.faultLocation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.config.DigitalInfo;

public class M9kLineLogicFilter {
	Map<String, DigitalInfo> abnormalEvents = null;; 
	List<String> lstAbnormalEvents = null;
	  int i=0;
	  int recursiveCount = 0;
	  boolean ans=false;
	  boolean ans2=false;
	  boolean ans1 = false;
	  int eqnLength = 0;
	  static int startIndex = -1; 
	  static int endIndex = -1; 
	  private static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kLineLogicFilter.class);
	public boolean isLogicTrue(Map<String, DigitalInfo> abnormalEvents, String decisionLogic) throws M9000Exception 
	{
		boolean isLogic = true;
		this.abnormalEvents = abnormalEvents;
		String replacedLogic = replaceAlternativeOperators(decisionLogic);
		logger.debug("REsult string "+replacedLogic);
		replacedLogic = replaceMembersInLogicWithOneOrZero(replacedLogic);
		logger.debug("Replaced logic "+replacedLogic);
//		logger.debug((1&1&1&(0|1&1))+" integer value "+String.valueOf(replacedLogic));
//		replacedLogic = replacedLogic.replaceAll("0", " false ");
//		replacedLogic = replacedLogic.replaceAll("1", " true ");
		logger.debug("Replaced logic "+replacedLogic);
		isLogic = isFilteredLogicTrue(replacedLogic);
		return isLogic;
	}

	public boolean isLogicTrue(List<String> lstAbnormalEvents, String decisionLogic) throws M9000Exception 
	{
		boolean isLogic = true;
		this.lstAbnormalEvents = lstAbnormalEvents;
		String replacedLogic = replaceAlternativeOperators(decisionLogic);
		logger.debug("REsult string "+replacedLogic);
		replacedLogic = replaceMembersInLogicWithOneOrZero(replacedLogic);
		logger.debug("Replaced logic "+replacedLogic);
//		logger.debug((1&1&1&(0|1&1))+" integer value "+String.valueOf(replacedLogic));
//		replacedLogic = replacedLogic.replaceAll("0", " false ");
//		replacedLogic = replacedLogic.replaceAll("1", " true ");
		logger.debug("Replaced logic "+replacedLogic);
		isLogic = isFilteredLogicTrue(replacedLogic);
		return isLogic;
	}

	private String replaceAlternativeOperators(String strLogic)
	{
		String resultLogic = strLogic.toUpperCase();
		// replace alternatives for OR operation
		resultLogic = resultLogic.replace("OR", "|" +
				"");
		// 02-Nov-2022 - changed it to replaceAll as it is replacing regex
		resultLogic = resultLogic.replaceAll("\\+", "|");

	  // replace alternatives for AND operation
		resultLogic = resultLogic.replace("AND", "&");
		// 02-Nov-2022 - changed it to replaceAll as it is replacing regex
		resultLogic = resultLogic.replaceAll("\\*", "&");

	  // replace alternatives for NOT operation
		resultLogic = resultLogic.replace("NOT", "!");
		// 02-Nov-2022 - changed it to replaceAll as it is replacing regex
		resultLogic = resultLogic.replaceAll("\\s","");
		return resultLogic;
	}
	
	private String replaceMembersInLogicWithOneOrZero(String logic)
	{
		StringBuffer resultLogic = new StringBuffer();
	   int i;
	   StringBuffer member;

	   for (i=0;i<logic.length();) {
	     if (isLogicChar(logic.charAt(i))) 
	     {
	    	 resultLogic.append(logic.charAt(i++));
	    	 continue;
	     }
//	     member= getOneMember(i,logic);
	     member = new StringBuffer();
	     do
	     {
	    	 member.append(logic.charAt(i++));
	     }
	     while(i<logic.length() && !isLogicChar(logic.charAt(i)));
	     
	     if (!isNameValid(member)) 
	     {
	    	 logger.debug("Ignoring Invalid member "+member);
	     }
	     resultLogic.append(isMember(member.toString()) ? '1' : '0');
//	     logic[j++]= IsMember(member) ? '1' : '0';
	   } //end for

//	   logic[j]='\0';  //end the logic string
	   return resultLogic.toString();

	} //end ReplaceMembersInLogicWithOneOrZero
	
	private boolean isLogicChar(char c)
	{
	   switch (c) {case '(':case ')':case '!': return true;}

	   return isTwoSidesOpChar(c);

	} //end IsLogicChar
	//---------------------------------------------------------------------------
	//'!' is single side operant will be false here
	private boolean isTwoSidesOpChar(char c)
	{
	  switch (c) {case '|':case '&': return true;}

	  return false;

	} //end IsOpChar
	
	//return true if it is a valid member name else false
	private boolean isNameValid(StringBuffer member)
	{
	   int i;
	   
	   //0 or 1 are always members
	   if (member.toString().equalsIgnoreCase("0")|| member.toString().equalsIgnoreCase("1")) {
		   return true;
	   }
	   if (!Character.isLetter(member.charAt(0)))
	   {
		   return false;
	   }

	   //the 2nd char on has to be number
	   i=1;
	   while (i < member.length()) 
	   {
		   if (!Character.isDigit(member.charAt(i))) 
		   {
			   return false; 
		   }
		   i++;
			
	   }

	   return true;

	} //end IsNameValid

	//return true if member else false (1 is always a member, 0 and "" are always non-member)
	private boolean isMember(String member)
	{
		boolean isMember = false;
	  //1 and 0 are always members
	  if (member.equalsIgnoreCase("1"))
	  {
		  return true;
	  }
	  if (member=="0" || member.isEmpty()) return false;

	  if (abnormalEvents != null)
	  {
		  isMember = abnormalEvents.containsKey(member);
	  }
	  else if (lstAbnormalEvents != null)
	  {
		  isMember = lstAbnormalEvents.contains(member);
	  }
	  return isMember;

	} //end IsMember

	//---------------------------------------------------------------------------
	//---------------------------------------------------------------------------
//	        Functions for IsFilteredLogicTrue
	//---------------------------------------------------------------------------
	private boolean isFilteredLogicTrue(String logic) throws M9000Exception
	{
	  i=0;
	  ans=false;


	  eqnLength = logic.length();
	  //empty string is default to false 
	  //because logic is used to make decision for Fault Location calculation and
	  //and sending fault file to master. Default to true done less harm than false.
	  // CHANGED 4-24-2009
	  if (eqnLength<1) return false;

	  //if equation invalid in some way return true, same reason as empty string above
	  ans = eqnOpen(logic);

	  if (i<eqnLength)
	    throw new M9000Exception("IsFilteredLogicTrue:: char#("+i+") in '"+logic+"' is invalid there.");

	  return ans;

	} //end IsFilteredLogicTrue
	//---------------------------------------------------------------------------
	//Only used after Logic members filter to 1 or 0
	//IsPointOutOfEqnLength is to prevent the pointer i point out of the logic equation
	//which could cause crash of the computer
	private boolean isPointOutOfEqnLength(int i) throws M9000Exception
	{
	   if (i<eqnLength)
	   {
		   return true;
	   }

	   throw new M9000Exception(" i("+i+") >= eqnLength("+eqnLength+")");

	} //end CheckPointOutOfEqnLength
	//---------------------------------------------------------------------------
	//Only used after Logic members filter to 1 or 0
	//Only called by IsFilterLogicTrue
	//ans1 ... but x1 could be the whole thing
	//return 0 on success else 1
	private boolean eqnOpen(String eqline) throws M9000Exception
	{
	   boolean ans1,ans2;

	   if (!isPointOutOfEqnLength(i)) 
	   {
		   throw new M9000Exception("Invalid equation");
	   }

	   ans1 = wantAnswer(eqline);
//	   if (!ans1) return false; //a member or (...) are both consider close

	   while (i < eqnLength && eqline.charAt(i)!=')') {
	     switch (eqline.charAt(i)) {
	       case '|': 
	    	   {
	    		   ++i;
	    		   ans2 = eqnOpen(eqline);
	    		   ans1 = ans1 || ans2; 
	    		   break;
	    	   }
	       case '&': 
	    	   {
	    		   ++i;
	    		   ans2 = eqnOpen(eqline);
	    		   ans1 = ans1 && ans2; 
	    		   break;
	    	   }
	       default:
	         throw new M9000Exception("LogicFilter char#("+i+") in '"+eqline+"' is invalid there.");
	     } //end switch (eqline.charAt(i))
	   } //end while (eqline.charAt(i)!='\0' && eqline.charAt(i)!=')')

	   ans = ans1;

	   return ans;

	} //end eqnOpen
	//---------------------------------------------------------------------------
	//Only used after Logic members filter to 1 or 0
	//the function that call wantAnswer must be looking for an answer
	//i.e. not a eqnOpen but can be EqnClose (...) or members
	//return 0 on success else 1
	private boolean wantAnswer(String eqline) throws M9000Exception
	{
	  boolean ans2;
	  
	  if (!isPointOutOfEqnLength(i)) 
	  {
		  throw new M9000Exception("Invalid equation");
	  }

	  switch (eqline.charAt(i)) {
	    case '1': 
	    	{
	    		++i; 
	    		return true;
	    	}
	    case '0': 
	    	{
	    		++i; 
	    		return false;
	    	}
	    case '!': 
	    	{
	    		++i;
	    		ans2 = wantAnswer(eqline);
	    		return !ans2;
	    	}
	    case '(': 
	    	{
	    		return eqnClose(eqline);
	    	}
	    default:
	      throw new M9000Exception("LogicFilter:: char#("+i+") in '"+eqline+"' is invalid there.");
	  } //end switch (eqline.charAt(i))

	} //end wantAnswer
	//---------------------------------------------------------------------------
	//Only used after Logic members filter to 1 or 0
	//return 0 on success else 1
	private boolean eqnClose(String eqline) throws M9000Exception
	{
	  if (!isPointOutOfEqnLength(i))
	  {
		  	throw new M9000Exception("Invalid equation");
	  }

	  if (eqline.charAt(i)!='(')
		  throw new M9000Exception("LogicFilter:: char#("+i+") in '"+eqline+"' has to be '('");

	  //it was '(' so go to next char
	  ++i;

	  ans = eqnOpen(eqline); 

	  if (i == eqnLength || eqline.charAt(i) != ')')
	  {
		  throw new M9000Exception("LogicFilter:: char#("+i+") in '"+eqline+"' has to be ')'");
	  }

	  //it is ')'  then point at next location
	  ++i;

	  //check the next char before return
	  //next char has to be OpChar e.g. '|' or '&' or end-of-line
	  if (i == eqnLength || isTwoSidesOpChar(eqline.charAt(i)) || eqline.charAt(i)==')')
	  {
		  return ans;
	  }

	  throw new M9000Exception("LogicFilter:: char#("+i+") in '"+eqline+"' is invalid there.");

	} //end EqnClose
	//---------------------------------------------------------------------------

//	private void isFilterLogicTrue(int index, String logic)
//	{
//		recursiveCount++;
//		logger.debug(" Index passed "+index);
//		int start= logic.indexOf("(", index);
//		int end= logic.indexOf(")", index);
//		logger.debug("start value "+start);
//		logger.debug("end value "+end);
//		if (start != -1 && start< end)
//		{
//			isFilterLogicTrue((start+1), logic);
//			logger.debug("Returning index "+index+" start "+start +" end index "+end );
//				String substring = logic.substring(start+1, end);
//				logger.debug("\n\t\t\trecursiveCount "+recursiveCount-- +" - "+substring+"\n");
//		}
//		else if (start != -1 && start > end)
//		{
//			logger.debug("Else If ");
//			isFilterLogicTrue((end+1), logic);
//		}
//	}
	
	public static void main(String[] args)
	{
		M9kLineLogicFilter m9kLineLogicFilter = new M9kLineLogicFilter();
		String logic = "((T1 OR T2 OR T3) AND (E1 AND E2 AND E4))";
		Map<String, DigitalInfo> abnormalEvents = new HashMap<String, DigitalInfo>(); 
		abnormalEvents.put("T1", new DigitalInfo(0));
		abnormalEvents.put("T2", new DigitalInfo(1));
		abnormalEvents.put("T3", new DigitalInfo(2));
		abnormalEvents.put("E1", new DigitalInfo(3));
		abnormalEvents.put("E2", new DigitalInfo(4));
		abnormalEvents.put("E4", new DigitalInfo(5));
		try {
			logger.debug("Is Logic passed? "+m9kLineLogicFilter.isLogicTrue(abnormalEvents, logic));
		} catch (M9000Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		logger.debug("Input Logic "+logic);
//		m9kLineLogicFilter.isFilterLogicTrue(logic);
//		logger.debug("Returning  startIndex "+startIndex+" end index "+endIndex);
	}

}
