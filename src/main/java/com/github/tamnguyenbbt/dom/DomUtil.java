package com.github.tamnguyenbbt.dom;

import com.github.tamnguyenbbt.exception.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * <h1> Html DOM Utility </h1>
 *
 * Generate xpath and find jsoup element based on the closest element (anchor element) information.
 * Useful for back-end web service resource testing and web front end Selenium-based testing
 *
 * @author: Tam Nguyen - tamnguyennb@gmail.com - https://www.linkedin.com/in/tam-nguyen-a0792930
 * @version: 1.0
 * @since: 2018-09-30
 */
public class DomUtil
{
    /**
     * get jsoup document from a browser managed by Selenium WebDriver
     *
     * @param driver Selenium WebDriver
     * @return jsoup document. Null if driver is null
     */
    public static Document getActiveDocument(WebDriver driver)
    {
        if(driver == null)
        {
            return null;
        }

        WebElement htmlElement = driver.findElement(By.xpath("//html"));
        String htmlContent = htmlElement.getAttribute("innerHTML");
        return DomUtil.getDocument(htmlContent);
    }

    /**
     * get jsoup document from a file. jsoup parsing file with UTF-8 charset
     *
     * @param path path to html file
     * @return jsoup document. Null if the file not exist
     * @exception IOException
     */
    public static Document htmlFileToDocument(String path) throws IOException
    {
        return htmlFileToDocument(path, "UTF-8");
    }

    /**
     * get jsoup document from a file
     *
     * @param path path to html file
     * @param charsetName charset name used by jsoup parsing
     * @return jsoup document. Null if the file not exist
     * @exception IOException
     */
    public static Document htmlFileToDocument(String path, String charsetName) throws IOException
    {
        File file = new File(path);
        Document document = null;

        if(file.exists() && file.isFile())
        {
            document = Jsoup.parse(file, charsetName);
        }

        return document;
    }

    /**
     * get jsoup document from html content
     *
     * @param htmlContent html content string
     * @return jsoup document
     */
    public static Document getDocument(String htmlContent)
    {
        return Jsoup.parse(htmlContent);
    }

    /**
     * get Selenium WebElement from a browser managed by Selenium WebDriver
     *
     * @param driver Selenium WebDriver
     * @param anchorElementOwnText own text of an html anchor tag, excluding spaces and tabs.
     *                             The anchor element is an unique html element closest to the web element to search.
     *                             The anchor element should be easy to be located on a web page with unique text such as a label
     * @param searchCssQuery css query of the element to search
     * @return Selenium WebElement based on the minimal DOM distance from possible found elements to anchor element.
     *         Null if no element found by the provided searchCssQuery
     * @exception NoAnchorElementFoundException when no anchor element is found by @anchorElementOwnText
     * @exception AmbiguousAnchorElementsException when more than one anchor elements are found by @anchorElementOwnText
     * @exception AmbiguousFoundWebElementsException when more than one search elements are found by @searchCssQuery.
     *                                               This occurs when the found elements having the same DOM distances to the anchor element
     */
    public static WebElement findElement(
            WebDriver driver,
            String anchorElementOwnText,
            String searchCssQuery)
            throws NoAnchorElementFoundException, AmbiguousAnchorElementsException, AmbiguousFoundWebElementsException
    {
        return findElement(driver, null, anchorElementOwnText, searchCssQuery);
    }

    /**
     * get Selenium WebElement from a browser managed by Selenium WebDriver
     *
     * @param driver Selenium WebDriver
     * @param anchorElementTagName html anchor tag
     * @param anchorElementOwnText own text of the html anchor tag, excluding spaces and tabs.
     *                             The anchor element is an unique html element closest to the web element to search.
     *                             The anchor element should be easy to be located on a web page with unique text such as a label
     * @param searchCssQuery css query of the element to search
     * @return Selenium WebElement based on the minimal DOM distance from possible found element to anchor element.
     *         Null if no element found by the provided searchCssQuery
     * @exception NoAnchorElementFoundException when no anchor element is found by @anchorElementOwnText
     * @exception AmbiguousAnchorElementsException when more than one anchor elements are found by @anchorElementOwnText
     * @exception AmbiguousFoundWebElementsException when more than one search elements are found by @searchCssQuery.
     *                                               This occurs when the found elements having the same DOM distances to the anchor element
     */
    public static WebElement findElement(
            WebDriver driver,
            String anchorElementTagName,
            String anchorElementOwnText,
            String searchCssQuery)
            throws NoAnchorElementFoundException, AmbiguousAnchorElementsException, AmbiguousFoundWebElementsException
    {
        Document document = DomUtil.getActiveDocument(driver);

        if(document == null)
        {
            return null;
        }

        String xpath;

        try
        {
            xpath = getXPath(document, anchorElementTagName, anchorElementOwnText, searchCssQuery);
        }
        catch(AmbiguousFoundXPathsException e)
        {
            throw new AmbiguousFoundWebElementsException("More than one web element found");
        }

        return xpath == null ? null : driver.findElement(By.xpath(xpath));
    }

    /**
     * get Selenium WebElement from a browser managed by Selenium WebDriver
     *
     * @param driver Selenium WebDriver
     * @param anchorElementInfo information about anchor element.
     *                          The anchor element is an unique html element closest to the web element to search.
     *                          The anchor element should be easy to be located on a web page with unique text such as a label
     * @param searchCssQuery css query of the element to search
     * @return Selenium WebElement based on the minimal DOM distance from possible found element to anchor element.
     *         Null if no element found by the provided searchCssQuery
     * @exception AnchorIndexIfMultipleFoundOutOfBoundException when the indexIfMultipleFound property of @anchorElementInfo is out of bound
     * @exception NoAnchorElementFoundException when no anchor element is found by @anchorElementOwnText
     * @exception AmbiguousAnchorElementsException when more than one anchor elements are found by @anchorElementOwnText
     * @exception AmbiguousFoundWebElementsException when more than one search elements are found by @searchCssQuery.
     *                                               This occurs when the found elements having the same DOM distances to the anchor element
     */
    public static WebElement findElement(
            WebDriver driver,
            ElementInfo anchorElementInfo,
            String searchCssQuery)
            throws AnchorIndexIfMultipleFoundOutOfBoundException, NoAnchorElementFoundException, AmbiguousAnchorElementsException, AmbiguousFoundWebElementsException
    {
        Document document = DomUtil.getActiveDocument(driver);

        if(document == null)
        {
            return null;
        }

        String xpath;

        try
        {
            xpath = getXPath(document, anchorElementInfo, searchCssQuery);
        }
        catch(AmbiguousFoundXPathsException e)
        {
            throw new AmbiguousFoundWebElementsException("More than one web element found");
        }

        return xpath == null ? null : driver.findElement(By.xpath(xpath));
    }

    /**
     * get xpath of the element in jsoup document closest to anchor element
     *
     * @param document jsoup Document
     * @param anchorElementOwnText own text of a html anchor tag, excluding spaces and tabs.
     *                             The anchor element is an unique html element closest to the web element to search.
     *                             The anchor element should be easy to be located on a web page with unique text such as a label
     * @param searchCssQuery css query of the element to search
     * @return xpath string based on the minimal DOM distance from possible found element to anchor element.
     *         Null if no xpath found by the provided searchCssQuery
     * @exception NoAnchorElementFoundException when no anchor element is found by @anchorElementOwnText
     * @exception AmbiguousAnchorElementsException when more than one anchor elements are found by @anchorElementOwnText
     * @exception AmbiguousFoundXPathsException when more than one search xpaths are found by @searchCssQuery.
     *                                               This occurs when the found elements having the same DOM distances to the anchor element
     */
    public static String getXPath(
            Document document,
            String anchorElementOwnText,
            String searchCssQuery)
            throws NoAnchorElementFoundException, AmbiguousAnchorElementsException, AmbiguousFoundXPathsException
    {
        return getXPath(document, null, anchorElementOwnText, searchCssQuery);
    }

    /**
     * get xpath of the element in jsoup document closest to anchor element
     *
     * @param document jsoup Document
     * @param anchorElementTagName html anchor tag
     * @param anchorElementOwnText own text of the html anchor tag, excluding spaces and tabs.
     *                             The anchor element is an unique html element closest to the web element to search.
     *                             The anchor element should be easy to be located on a web page with unique text such as a label
     * @param searchCssQuery css query of the element to search
     * @return xpath string based on the minimal DOM distance from possible found element to anchor element.
     *         Null if no xpath found by the provided searchCssQuery
     * @exception NoAnchorElementFoundException when no anchor element is found by @anchorElementOwnText
     * @exception AmbiguousAnchorElementsException when more than one anchor elements are found by @anchorElementOwnText
     * @exception AmbiguousFoundXPathsException when more than one search xpaths are found by @searchCssQuery.
     *                                               This occurs when the found elements having the same DOM distances to the anchor element
     */
    public static String getXPath(
            Document document,
            String anchorElementTagName,
            String anchorElementOwnText,
            String searchCssQuery)
            throws NoAnchorElementFoundException, AmbiguousAnchorElementsException, AmbiguousFoundXPathsException
    {
        List<String> xpaths = getXPaths(document, anchorElementTagName, anchorElementOwnText, searchCssQuery);

        if(xpaths.size() > 1)
        {
            throw new AmbiguousFoundXPathsException("More than one xpaths found");
        }

        return hasItem(xpaths) ? xpaths.get(0) : null;
    }

    /**
     * get xpath of the element in jsoup document closest to anchor element
     *
     * @param document jsoup Document
     * @param anchorElementInfo information about anchor element.
     *                          The anchor element is an unique html element closest to the web element to search.
     *                          The anchor element should be easy to be located on a web page with unique text such as a label
     * @param searchCssQuery css query of the element to search
     * @return xpath string based on the minimal DOM distance from possible found element to anchor element.
     *         Null if no xpath found by the provided searchCssQuery
     * @exception AnchorIndexIfMultipleFoundOutOfBoundException when the indexIfMultipleFound property of @anchorElementInfo is out of bound
     * @exception NoAnchorElementFoundException when no anchor element is found by @anchorElementOwnText
     * @exception AmbiguousAnchorElementsException when more than one anchor elements are found by @anchorElementOwnText
     * @exception AmbiguousFoundXPathsException when more than one search xpaths are found by @searchCssQuery.
     *                                               This occurs when the found elements having the same DOM distances to the anchor element
     */
    public static String getXPath(
            Document document,
            ElementInfo anchorElementInfo,
            String searchCssQuery)
            throws AnchorIndexIfMultipleFoundOutOfBoundException, NoAnchorElementFoundException, AmbiguousAnchorElementsException, AmbiguousFoundXPathsException
    {
        List<String> xpaths = getXPaths(document, anchorElementInfo, searchCssQuery);

        if(xpaths.size() > 1)
        {
            throw new AmbiguousFoundXPathsException("More than one xpaths found");
        }

        return hasItem(xpaths) ? xpaths.get(0) : null;
    }

    /**
     * get xpath of the element in jsoup document closest to anchor element
     *
     * @param anchorElement unique jsoup element in DOM document.
     *                      The anchor element should be easy to be located on a web page with unique text such as a label
     * @param searchElements possible jsoup element candidates to search from
     * @return xpath string based on the minimal DOM distance from possible found element to anchor element.
     *         Null if no xpath found
     * @exception AmbiguousFoundXPathsException when more than one search xpaths are found.
     *                                          This occurs when the found elements having the same DOM distances to the anchor element
     */
    public static String getXPath(Element anchorElement, Elements searchElements)
            throws AmbiguousFoundXPathsException
    {
        List<String> xpaths = getXPaths(anchorElement, searchElements);

        if(xpaths.size() > 1)
        {
            throw new AmbiguousFoundXPathsException("More than one xpaths found");
        }

        return hasItem(xpaths) ? xpaths.get(0) : null;
    }

    /**
     * get xpath list of the elements in jsoup document closest to anchor element
     *
     * @param document jsoup Document
     * @param anchorElementOwnText own text of a html anchor tag, excluding spaces and tabs.
     *                             The anchor element is an unique html element closest to the web element to search.
     *                             The anchor element should be easy to be located on a web page with unique text such as a label
     * @param searchCssQuery css query of the element to search
     * @return xpath list based on the minimal DOM distance from possible found elements to anchor element.
     *         When there are more than one elements having the same minimal DOM distance to the anchor element, their xpaths are returned.
     *         Empty list if no xpath found by the provided searchCssQuery
     * @exception NoAnchorElementFoundException when no anchor element is found by @anchorElementOwnText
     * @exception AmbiguousAnchorElementsException when more than one anchor elements are found by @anchorElementOwnText
     */
    public static List<String> getXPaths(
            Document document,
            String anchorElementOwnText,
            String searchCssQuery)
            throws NoAnchorElementFoundException, AmbiguousAnchorElementsException
    {
        return getXPaths(document, null, anchorElementOwnText, searchCssQuery);
    }

    /**
     * get xpath list of the elements in jsoup document closest to anchor element
     *
     * @param document jsoup Document
     * @param anchorElementInfo information about the anchor element.
     *                          The anchor element is an unique html element closest to the web element to search.
     *                          The anchor element should be easy to be located on a web page with unique text such as a label
     * @param searchCssQuery css query of the element to search
     * @return xpath list based on the minimal DOM distance from possible found elements to anchor element.
     *         When there are more than one elements having the same minimal DOM distance to the anchor element, their xpaths are returned.
     *         Empty list if no xpath found by the provided searchCssQuery
     * @exception AnchorIndexIfMultipleFoundOutOfBoundException when the indexIfMultipleFound property of @anchorElementInfo is out of bound
     * @exception NoAnchorElementFoundException when no anchor element is found by @anchorElementOwnText
     * @exception AmbiguousAnchorElementsException when more than one anchor elements are found by @anchorElementOwnText
     */
    public static List<String> getXPaths(
            Document document,
            ElementInfo anchorElementInfo,
            String searchCssQuery)
            throws AnchorIndexIfMultipleFoundOutOfBoundException, NoAnchorElementFoundException, AmbiguousAnchorElementsException
    {
        List<Element> anchorElements = getElements(document, anchorElementInfo);
        List<Element> activeAnchorElements = getActiveAnchorElements(anchorElementInfo, anchorElements);
        return getXPaths(document, activeAnchorElements, searchCssQuery);
    }

    /**
     * get xpath list of the elements in jsoup document closest to anchor element
     *
     * @param document jsoup Document
     * @param anchorElementTagName html anchor tag
     * @param anchorElementOwnText own text of the html anchor tag, excluding spaces and tabs.
     *                             The anchor element is an unique html element closest to the web element to search.
     *                             The anchor element should be easy to be located on a web page with unique text such as a label
     * @param searchCssQuery css query of the element to search
     * @return xpath list based on the minimal DOM distance from possible found elements to anchor element.
     *         When there are more than one elements having the same minimal DOM distance to the anchor element, their xpaths are returned.
     *         Empty list if no xpath found by the provided searchCssQuery
     * @exception NoAnchorElementFoundException when no anchor element is found by @anchorElementOwnText
     * @exception AmbiguousAnchorElementsException when more than one anchor elements are found by @anchorElementOwnText
     */
    public static List<String> getXPaths(
            Document document,
            String anchorElementTagName,
            String anchorElementOwnText,
            String searchCssQuery)
            throws NoAnchorElementFoundException, AmbiguousAnchorElementsException
    {
        List<Element> anchorElements = getElements(document, anchorElementTagName, anchorElementOwnText);
        return getXPaths(document, anchorElements, searchCssQuery);
    }

    /**
     * get xpath list of the elements closest to anchor element
     *
     * @param anchorElement unique jsoup element in DOM document.
     *                      The anchor element should be easy to be located on a web page with unique text such as a label
     * @param searchElements possible jsoup element candidates to search from
     * @return xpath list based on the minimal DOM distance from possible found elements to anchor element.
     *         When there are more than one elements having the same minimal DOM distance to the anchor element, their xpaths are returned.
     *         Empty list if no xpath found
     */
    public static List<String> getXPaths(Element anchorElement, Elements searchElements)
    {
        List<String> xpathList = new ArrayList<>();
        List<SearchElementRecord> foundElementRecords = getClosestSearchElementsFromAnchorElement(anchorElement, searchElements);

        if(hasNoItem(foundElementRecords))
        {
            return xpathList;
        }

        for(SearchElementRecord record : foundElementRecords)
        {
            String xpath = buildXpath(record, anchorElement);

            if(xpath != null)
            {
                xpathList.add(xpath);
            }
        }

        return xpathList;
    }

    /**
     * get jsoup element from jsoup document closest to anchor element
     *
     * @param document jsoup Document
     * @param anchorElementOwnText own text of a html anchor tag, excluding spaces and tabs.
     *                             The anchor element is an unique html element closest to the web element to search.
     *                             The anchor element should be easy to be located on a web page with unique text such as a label
     * @param searchCssQuery css query of the element to search
     * @return jsoup based on the minimal DOM distance from possible found element to anchor element.
     *         Null if no element found by the provided searchCssQuery
     * @exception NoAnchorElementFoundException when no anchor element is found by @anchorElementOwnText
     * @exception AmbiguousAnchorElementsException when more than one anchor elements are found by @anchorElementOwnText
     * @exception AmbiguousFoundElementsException when more than one search elements are found by @searchCssQuery.
     *                                               This occurs when the found elements having the same DOM distances to the anchor element
     */
    public static Element getClosestElement(
            Document document,
            String anchorElementOwnText,
            String searchCssQuery)
            throws NoAnchorElementFoundException, AmbiguousAnchorElementsException, AmbiguousFoundElementsException
    {
        return getClosestElement(document, null, anchorElementOwnText, searchCssQuery);
    }

    /**
     * get jsoup element from jsoup document closest to anchor element
     *
     * @param document jsoup Document
     * @param anchorElementTagName html anchor tag
     * @param anchorElementOwnText own text of the html anchor tag, excluding spaces and tabs.
     *                             The anchor element is an unique html element closest to the web element to search.
     *                             The anchor element should be easy to be located on a web page with unique text such as a label
     * @param searchCssQuery css query of the element to search
     * @return jsoup based on the minimal DOM distance from possible found element to anchor element.
     *         Null if no element found by the provided searchCssQuery
     * @exception NoAnchorElementFoundException when no anchor element is found by @anchorElementOwnText
     * @exception AmbiguousAnchorElementsException when more than one anchor elements are found by @anchorElementOwnText
     * @exception AmbiguousFoundElementsException when more than one search elements are found by @searchCssQuery.
     *                                               This occurs when the found elements having the same DOM distances to the anchor element
     */
    public static Element getClosestElement(
            Document document,
            String anchorElementTagName,
            String anchorElementOwnText,
            String searchCssQuery)
            throws NoAnchorElementFoundException, AmbiguousAnchorElementsException, AmbiguousFoundElementsException
    {
        List<Element> foundElement = getClosestElements(document, anchorElementTagName, anchorElementOwnText, searchCssQuery);

        if(foundElement.size() > 1)
        {
            throw new AmbiguousFoundElementsException("More than one elements found");
        }

        return hasItem(foundElement) ? foundElement.get(0) : null;
    }

    /**
     * get jsoup element from jsoup document closest to anchor element
     *
     * @param document jsoup Document
     * @param anchorElementInfo information about the anchor element.
     *                          The anchor element is an unique html element closest to the web element to search.
     *                          The anchor element should be easy to be located on a web page with unique text such as a label
     * @param searchCssQuery css query of the element to search
     * @return jsoup based on the minimal DOM distance from possible found element to anchor element.
     *         Null if no element found by the provided searchCssQuery
     * @exception AnchorIndexIfMultipleFoundOutOfBoundException when the indexIfMultipleFound property of @anchorElementInfo is out of bound
     * @exception NoAnchorElementFoundException when no anchor element is found by @anchorElementOwnText
     * @exception AmbiguousAnchorElementsException when more than one anchor elements are found by @anchorElementOwnText
     * @exception AmbiguousFoundElementsException when more than one search elements are found by @searchCssQuery.
     *                                               This occurs when the found elements having the same DOM distances to the anchor element
     */
    public static Element getClosestElement(
            Document document,
            ElementInfo anchorElementInfo,
            String searchCssQuery)
            throws AnchorIndexIfMultipleFoundOutOfBoundException, NoAnchorElementFoundException, AmbiguousAnchorElementsException, AmbiguousFoundElementsException
    {
        List<Element> foundElement = getClosestElements(document, anchorElementInfo, searchCssQuery);

        if(foundElement.size() > 1)
        {
            throw new AmbiguousFoundElementsException("More than one elements found");
        }

        return hasItem(foundElement) ? foundElement.get(0) : null;
    }

    /**
     * get jsoup element closest to anchor element
     *
     * @param anchorElement unique jsoup element in DOM document.
     *                      The anchor element should be easy to be located on a web page with unique text such as a label
     * @param searchElements possible jsoup element candidates to search from
     * @return jsoup element based on the minimal DOM distance from possible found element to anchor element.
     *         Null if no element found
     */
    public static Element getClosestElement(Element anchorElement, Elements searchElements)
            throws AmbiguousFoundElementsException
    {
        List<Element> foundElement = getClosestElements(anchorElement, searchElements);

        if(foundElement.size() > 1)
        {
            throw new AmbiguousFoundElementsException("More than one elements found");
        }

        return hasItem(foundElement) ? foundElement.get(0) : null;
    }

    /**
     * get jsoup element list from jsoup document closest to anchor element
     *
     * @param document jsoup Document
     * @param anchorElementOwnText own text of a html anchor tag, excluding spaces and tabs.
     *                             The anchor element is an unique html element closest to the web element to search.
     *                             The anchor element should be easy to be located on a web page with unique text such as a label
     * @param searchCssQuery css query of the element to search
     * @return jsoup element list based on the minimal DOM distance from possible found elements to anchor element.
     *         When there are more than one elements having the same minimal DOM distance to the anchor element, they are returned.
     *         Empty list if no element found by the provided searchCssQuery
     * @exception NoAnchorElementFoundException when no anchor element is found by @anchorElementOwnText
     * @exception AmbiguousAnchorElementsException when more than one anchor elements are found by @anchorElementOwnText
     */
    public static List<Element> getClosestElements(
            Document document,
            String anchorElementOwnText,
            String searchCssQuery)
            throws NoAnchorElementFoundException, AmbiguousAnchorElementsException
    {
        return getClosestElements(document, null, anchorElementOwnText, searchCssQuery);
    }

    /**
     * get jsoup element list from jsoup document closest to anchor element
     *
     * @param document jsoup Document
     * @param anchorElementTagName html anchor tag
     * @param anchorElementOwnText own text of the html anchor tag, excluding spaces and tabs.
     *                             The anchor element is an unique html element closest to the web element to search.
     *                             The anchor element should be easy to be located on a web page with unique text such as a label
     * @param searchCssQuery css query of the element to search
     * @return jsoup element list based on the minimal DOM distance from possible found elements to anchor element.
     *         When there are more than one elements having the same minimal DOM distance to the anchor element, they are returned.
     *         Empty list if no element found by the provided searchCssQuery
     * @exception NoAnchorElementFoundException when no anchor element is found by @anchorElementOwnText
     * @exception AmbiguousAnchorElementsException when more than one anchor elements are found by @anchorElementOwnText
     */
    public static List<Element> getClosestElements(
            Document document,
            String anchorElementTagName,
            String anchorElementOwnText,
            String searchCssQuery)
            throws NoAnchorElementFoundException, AmbiguousAnchorElementsException
    {
        List<Element> anchorElements = getElements(document, anchorElementTagName, anchorElementOwnText);
        return getClosestElements(document, anchorElements, searchCssQuery);
    }

    /**
     * get jsoup element list from jsoup document closest to anchor element
     *
     * @param document jsoup Document
     * @param anchorElementInfo information about the anchor element.
     *                          The anchor element is an unique html element closest to the web element to search.
     *                          The anchor element should be easy to be located on a web page with unique text such as a label
     * @param searchCssQuery css query of the element to search
     * @return jsoup element list based on the minimal DOM distance from possible found elements to anchor element.
     *         When there are more than one elements having the same minimal DOM distance to the anchor element, they are returned.
     *         Empty list if no element found by the provided searchCssQuery
     * @exception AnchorIndexIfMultipleFoundOutOfBoundException when the indexIfMultipleFound property of @anchorElementInfo is out of bound
     * @exception NoAnchorElementFoundException when no anchor element is found by @anchorElementOwnText
     * @exception AmbiguousAnchorElementsException when more than one anchor elements are found by @anchorElementOwnText
     */
    public static List<Element> getClosestElements(
            Document document,
            ElementInfo anchorElementInfo,
            String searchCssQuery)
            throws AnchorIndexIfMultipleFoundOutOfBoundException, NoAnchorElementFoundException, AmbiguousAnchorElementsException
    {
        List<Element> anchorElements = getElements(document, anchorElementInfo);
        List<Element> activeAnchorElements = getActiveAnchorElements(anchorElementInfo, anchorElements);
        return getClosestElements(document, activeAnchorElements, searchCssQuery);
    }

    /**
     * get jsoup element list closest to anchor element
     *
     * @param anchorElement unique jsoup element in DOM document.
     *                      The anchor element should be easy to be located on a web page with unique text such as a label
     * @param searchElements possible jsoup element candidates to search from
     * @return jsoup element list based on the minimal DOM distance from possible found elements to anchor element.
     *         When there are more than one elements having the same minimal DOM distance to the anchor element, they are returned.
     *         Empty list if no element found
     */
    public static List<Element> getClosestElements(Element anchorElement, Elements searchElements)
    {
        List<Element> elements = new ArrayList<>();
        List<SearchElementRecord> foundElementRecords = getClosestSearchElementsFromAnchorElement(anchorElement, searchElements);

        if(hasItem(foundElementRecords))
        {
            for(SearchElementRecord record : foundElementRecords)
            {
                elements.add(record.element);
            }
        }

        return elements;
    }

    /**
     * get jsoup element list from jsoup document by tag name where the tag own text contains a pattern with case insensitive
     *
     * @param document DOM document
     * @param tagName tag name, case insensitive
     * @param pattern a string which the tag own text should contain, case insensitive. Spaces and tabs are ignored
     * @return jsoup element list
     */
    public static List<Element> getElementsByTagNameContainingOwnTextIgnoreCase(Document document, String tagName, String pattern)
    {
        return getElementsByTagNameMatchingOwnText(document, tagName, pattern, false, false, true);
    }

    /**
     * get jsoup element list from jsoup document by tag name where the tag own text contains a pattern with case sensitive
     *
     * @param document DOM document
     * @param tagName tag name, case insensitive
     * @param pattern a string which the tag own text should contain, case sensitive. Spaces and tabs are ignored
     * @return jsoup element list
     */
    public static List<Element> getElementsByTagNameContainingOwnText(Document document, String tagName, String pattern)
    {
        return getElementsByTagNameMatchingOwnText(document, tagName, pattern, true, false, true);
    }

    /**
     * get jsoup element list from jsoup document by tag name where the tag own text matches a pattern with case insensitive
     *
     * @param document DOM document
     * @param tagName tag name, case insensitive
     * @param pattern a string which the tag own text should match, case insensitive. Spaces and tabs are ignored
     * @return jsoup element list
     */
    public static List<Element> getElementsByTagNameMatchingOwnTextIgnoreCase(Document document, String tagName, String pattern)
    {
        return getElementsByTagNameMatchingOwnText(document, tagName, pattern, false, true, true);
    }

    /**
     * get jsoup element list from jsoup document by tag name where the tag own text matches a pattern with case sensitive
     *
     * @param document DOM document
     * @param tagName tag name, case insensitive
     * @param pattern a string which the tag own text should match, case sensitive. Spaces and tabs are ignored
     * @return jsoup element list
     */
    public static List<Element> getElementsByTagNameMatchingOwnText(Document document, String tagName, String pattern)
    {
        return getElementsByTagNameMatchingOwnText(document, tagName, pattern, true, true, true);
    }

    /**
     * get jsoup element list from jsoup document where the tag own text contains a pattern with case insensitive
     *
     * @param document DOM document
     * @param pattern a string which the tag own text should contain, case insensitive. Spaces and tabs are ignored
     * @return jsoup element list
     */
    public static List<Element> getElementsContainingOwnTextIgnoreCase(Document document, String pattern)
    {
        return getElementsMatchingOwnText(document, pattern, false, false, true);
    }

    /**
     * get jsoup element list from jsoup document where the tag own text contains a pattern with case sensitive
     *
     * @param document DOM document
     * @param pattern a string which the tag own text should contain, case sensitive. Spaces and tabs are ignored
     * @return jsoup element list
     */
    public static List<Element> getElementsContainingOwnText(Document document, String pattern)
    {
        return getElementsMatchingOwnText(document, pattern, true, false, true);
    }

    /**
     * get jsoup element list from jsoup document where the tag own text matches a pattern with case insensitive
     *
     * @param document DOM document
     * @param pattern a string which the tag own text should match, case insensitive. Spaces and tabs are ignored
     * @return jsoup element list
     */
    public static List<Element> getElementsMatchingOwnTextIgnoreCase(Document document, String pattern)
    {
        return getElementsMatchingOwnText(document, pattern, false, true, true);
    }

    /**
     * get jsoup element list from jsoup document where the tag own text matches a pattern with case sensitive
     *
     * @param document DOM document
     * @param pattern a string which the tag own text should match, case sensitive. Spaces and tabs are ignored
     * @return jsoup element list
     */
    public static List<Element> getElementsMatchingOwnText(Document document, String pattern)
    {
        return getElementsMatchingOwnText(document, pattern, true, true, true);
    }

    public static WebElement findWebElementWithTwoAnchors(WebDriver driver,
                                                          String parentAnchorElementOwnText,
                                                          String anchorElementOwnText,
                                                          String searchCssQuery)
    {
        return findWebElementWithTwoAnchors(driver,
                parentAnchorElementOwnText,
                null,
                anchorElementOwnText,
                searchCssQuery);
    }

    public static WebElement findWebElementWithTwoAnchors(WebDriver driver,
                                                          String parentAnchorElementOwnText,
                                                          String anchorElementTagName,
                                                          String anchorElementOwnText,
                                                          String searchCssQuery)
    {
        return findWebElementWithTwoAnchors(driver,
                null,
                parentAnchorElementOwnText,
                anchorElementTagName,
                anchorElementOwnText,
                searchCssQuery);
    }

    public static WebElement findWebElementWithTwoAnchors(WebDriver driver,
                                                          String parentAnchorElementTagName,
                                                          String parentAnchorElementOwnText,
                                                          String anchorElementTagName,
                                                          String anchorElementOwnText,
                                                          String searchCssQuery)
    {
        Elements anchorElements = findElements(driver,
                new ElementInfo(parentAnchorElementTagName, parentAnchorElementOwnText),
                new ElementInfo(anchorElementTagName, anchorElementOwnText));
        return findWebElement(driver, anchorElements, searchCssQuery);
    }

    public static WebElement findWebElementWithTwoAnchors(WebDriver driver,
                                                          ElementInfo parentAnchorElementInfo,
                                                          ElementInfo anchorElementInfo,
                                                          String searchCssQuery)
    {
        Elements anchorElements = findElements(driver, parentAnchorElementInfo, anchorElementInfo);
        return findWebElement(driver, anchorElements, searchCssQuery);
    }

    public static WebElement findWebElementHandlingPossibleMultipleAnchorsFound(WebDriver driver,
                                                                                String anchorElementOwnText,
                                                                                String searchCssQuery)
    {
        return findWebElementHandlingPossibleMultipleAnchorsFound(driver, null, anchorElementOwnText, searchCssQuery);
    }

    public static WebElement findWebElementHandlingPossibleMultipleAnchorsFound(WebDriver driver,
                                                                         String anchorElementTagName,
                                                                         String anchorElementOwnText,
                                                                         String searchCssQuery)
    {
        ElementInfo anchorElementInfo = new ElementInfo(anchorElementTagName, anchorElementOwnText);
        WebElement webElement = findWebElement(driver, anchorElementInfo, searchCssQuery);

        if(webElement == null)
        {
            anchorElementInfo.whereOwnTextContainingPattern = true;
            webElement = findWebElement(driver, anchorElementInfo, searchCssQuery);
        }

        return webElement;
    }

    public static WebElement findWebElement(WebDriver driver,
                                            String anchorElementOwnText,
                                            String searchCssQuery)
            throws NoAnchorElementFoundException, AmbiguousAnchorElementsException, AmbiguousFoundWebElementsException
    {
        return findWebElement(driver, null, anchorElementOwnText, searchCssQuery);
    }

    public static WebElement findWebElement(WebDriver driver,
                                            String anchorElementTagName,
                                            String anchorElementOwnText,
                                            String searchCssQuery)
            throws NoAnchorElementFoundException, AmbiguousAnchorElementsException, AmbiguousFoundWebElementsException
    {
        WebElement webElement = findElement(driver, anchorElementTagName, anchorElementOwnText, searchCssQuery);

        if(webElement == null)
        {
            ElementInfo anchorElementInfo = new ElementInfo(anchorElementTagName, anchorElementOwnText);
            anchorElementInfo.whereOwnTextContainingPattern = true;

            try
            {
                webElement = findElement(driver, anchorElementInfo, searchCssQuery);
            }
            catch(AnchorIndexIfMultipleFoundOutOfBoundException e){}
        }

        return webElement;
    }

    protected static List<Element> filterByPattern(List<Element> elements, String pattern, Condition condition)
    {
        List<Element> filtered = new ArrayList<>();

        if(hasItem(elements))
        {
            for (Element item : elements)
            {
                if(matchElementOwnText(
                        item,
                        pattern,
                        !condition.whereIgnoreCaseForOwnText,
                        !condition.whereOwnTextContainingPattern,
                        !condition.whereIncludingTabsAndSpacesForOwnText))
                {
                    filtered.add(item);
                }
            }
        }

        return filtered;
    }

    protected static Elements findElements(WebDriver driver, ElementInfo anchorElementInfo, ElementInfo searchElementInfo)
    {
        List<Element> result = new ArrayList<>();
        Document document = getActiveDocument(driver);
        List<Element> anchorElements = getElements(document, anchorElementInfo);
        Elements searchElements = toElements(getElements(document, searchElementInfo));

        if(hasItem(anchorElements) && hasItem(searchElements))
        {
            for (Element item : anchorElements)
            {
                result = getClosestElements(item, searchElements);

                if(hasItem(result))
                {
                    break;
                }
            }
        }

        return toElements(result);
    }

    protected static WebElement findWebElement(WebDriver driver, ElementInfo anchorElementInfo, String searchCssQuery)
    {
        Document document = getActiveDocument(driver);
        Elements anchorElements = toElements(getElements(document, anchorElementInfo));
        return findWebElement(driver, document, anchorElements, searchCssQuery);
    }

    protected static WebElement findWebElement(WebDriver driver, Elements anchorElements, String searchCssQuery)
    {
        Document document = getActiveDocument(driver);
        return findWebElement(driver, document, anchorElements, searchCssQuery);
    }

    protected static String buildXpath(SearchElementRecord record, Element anchorElement)
    {
        String xpath = null;
        Element rootElement = record.rootElement;
        Element foundElement = record.element;
        String xpathPartFromRootElementToFoundElement = buildXpathPartBetweenRootAndLeafExcludingRoot(rootElement, foundElement);
        String xpathPartFromRootElementToAnchorElement = buildXpathPartBetweenRootAndLeafExcludingRoot(rootElement, anchorElement);
        String rootElementTagName = rootElement.tagName();
        String anchorElementOwnText = anchorElement.ownText();

        if(xpathPartFromRootElementToFoundElement != null && xpathPartFromRootElementToAnchorElement != null)
        {
            if(xpathPartFromRootElementToAnchorElement == "" && xpathPartFromRootElementToFoundElement == "")
            {
                xpath =  String.format("//%s[contains(text(),'%s')]", rootElementTagName, anchorElementOwnText);
            }
            else if(xpathPartFromRootElementToAnchorElement == "")
            {
                xpath =  String.format("//%s[contains(text(),'%s')]/%s", rootElementTagName, anchorElementOwnText, xpathPartFromRootElementToFoundElement);
            }
            else if(xpathPartFromRootElementToFoundElement == "")
            {
                xpath =  String.format("//%s[%s[contains(text(),'%s')]]", xpathPartFromRootElementToAnchorElement, rootElementTagName, anchorElementOwnText);
            }
            else
            {
                xpath = String.format("//%s[%s[contains(text(),'%s')]]/%s",
                        rootElementTagName, xpathPartFromRootElementToAnchorElement, anchorElementOwnText, xpathPartFromRootElementToFoundElement);
            }
        }

        return xpath;
    }

    protected static List<Element> getActiveAnchorElements(ElementInfo anchorElementInfo, List<Element> anchorElements)
            throws AnchorIndexIfMultipleFoundOutOfBoundException
    {
        List<Element> activeAnchorElements = new ArrayList<>();

        if(anchorElementInfo != null)
        {
            if(anchorElementInfo.indexIfMultipleFound < 0 || anchorElements.isEmpty())
            {
                activeAnchorElements = anchorElements;
            }
            else if(anchorElementInfo.indexIfMultipleFound < anchorElements.size())
            {
                activeAnchorElements.add(anchorElements.get(anchorElementInfo.indexIfMultipleFound));
            }
            else
            {
                throw new AnchorIndexIfMultipleFoundOutOfBoundException("indexIfMultipleFound property of the AnchorElementInfo provided is out of bound");
            }
        }

        return activeAnchorElements;
    }

    protected static List<String> getXPaths(
            Document document,
            List<Element> anchorElements,
            String searchCssQuery)
            throws NoAnchorElementFoundException, AmbiguousAnchorElementsException
    {
        List<String> xpathList = new ArrayList<>();

        if(document == null)
        {
            return xpathList;
        }

        if(hasNoItem(anchorElements))
        {
            throw new NoAnchorElementFoundException("No anchor element found");
        }

        if(anchorElements.size() > 1)
        {
            throw new AmbiguousAnchorElementsException("More than one anchor elements found");
        }

        Element anchorElement = anchorElements.get(0);
        Elements searchElements = document.select(searchCssQuery);

        if(hasItem(searchElements) && anchorElement != null)
        {
            xpathList = getXPaths(anchorElement, searchElements);
        }

        return xpathList;
    }

    protected static List<Element> getClosestElements(
            Document document,
            List<Element> anchorElements,
            String searchCssQuery)
            throws NoAnchorElementFoundException, AmbiguousAnchorElementsException
    {
        List<Element> foundElements = new ArrayList<>();

        if(document == null)
        {
            return foundElements;
        }

        if(hasNoItem(anchorElements))
        {
            throw new NoAnchorElementFoundException("No anchor element found");
        }

        if(anchorElements.size() > 1)
        {
            throw new AmbiguousAnchorElementsException("More than one anchor elements found");
        }

        Element anchorElement = anchorElements.get(0);
        Elements searchElements = document.select(searchCssQuery);
        List<SearchElementRecord> foundElementRecords = getClosestSearchElementsFromAnchorElement(anchorElement, searchElements);

        if(hasItem(searchElements) && hasItem(foundElementRecords))
        {
            for(int i = 0; i <searchElements.size(); i++)
            {
                for(SearchElementRecord record : foundElementRecords)
                {
                    if(i == record.index)
                    {
                        foundElements.add(searchElements.get(i));
                        break;
                    }
                }
            }
        }

        return foundElements;
    }

    protected static List<Element> getElements(Document document, String elementTagName, String elementOwnText)
    {
        return elementTagName == null
                ? getElementsMatchingOwnText(document, elementOwnText)
                : getElementsByTagNameMatchingOwnText(document, elementTagName, elementOwnText);
    }

    protected static List<Element> getElements(Document document, ElementInfo elementInfo)
    {
        List<Element> elements = new ArrayList<>();

        if(elementInfo == null)
        {
            return elements;
        }

        return elementInfo.tagName == null
                ?   getElementsMatchingOwnText(
                        document,
                        elementInfo.ownText,
                        !elementInfo.whereIgnoreCaseForOwnText,
                        !elementInfo.whereOwnTextContainingPattern,
                        !elementInfo.whereIncludingTabsAndSpacesForOwnText)
                :   getElementsByTagNameMatchingOwnText(
                        document,
                        elementInfo.tagName,
                        elementInfo.ownText,
                        !elementInfo.whereIgnoreCaseForOwnText,
                        !elementInfo.whereOwnTextContainingPattern,
                        !elementInfo.whereIncludingTabsAndSpacesForOwnText);
    }

    protected static List<SearchElementRecord> getClosestSearchElementsFromAnchorElement(Element anchorElement, Elements searchElements)
    {
        List<SearchElementRecord> foundElementRecords = new ArrayList<>();

        if(hasNoItem(searchElements) || anchorElement == null)
        {
            return foundElementRecords;
        }

        for (int i = 0 ; i < searchElements.size(); i++)
        {
            Element currentSearchElement = searchElements.get(i);
            MapEntry<List<Integer>, List<TreeElement>> matchedElementPositionAndTreePair = buildTreeContainingBothSearchAndAnchorElements(anchorElement, currentSearchElement);

            if(matchedElementPositionAndTreePair != null)
            {
                List<TreeElement> tree = matchedElementPositionAndTreePair.getValue();
                List<Integer> currentSearchElementPosition = matchedElementPositionAndTreePair.getKey();
                TreeElement anchor = getFirstMatchedElementInTree(tree, anchorElement);

                if(hasItem(currentSearchElementPosition) && anchor != null && hasItem(anchor.position))
                {
                    SearchElementRecord currentFoundElementRecord = new SearchElementRecord();
                    currentFoundElementRecord.element = currentSearchElement;
                    currentFoundElementRecord.rootElement = getRootElement(tree).element;
                    currentFoundElementRecord.index = i;
                    currentFoundElementRecord.distanceToAnchorElement = currentSearchElementPosition.size() + anchor.position.size() - 2;
                    foundElementRecords.add(currentFoundElementRecord);
                }
            }
        }

        return getFoundElementRecordsWithShortestDistanceToAnchorElement(foundElementRecords);
    }

    /**
     * @return map entry of the position of the search element and the tree
     */
    protected static MapEntry<List<Integer>, List<TreeElement>> buildTreeContainingBothSearchAndAnchorElements(Element anchorElement, Element searchElement)
    {
        List<TreeElement> elementTree = new ArrayList<>();
        Element rootElement = anchorElement;
        TreeElement firstFound = null;

        while(firstFound == null)
        {
            elementTree = getElementTree(rootElement);
            firstFound = getFirstMatchedElementInTree(elementTree, searchElement);

            if(firstFound == null)
            {
                if(rootElement == null)
                {
                    break;
                }

                rootElement = rootElement.parent();
            }
        }

        return firstFound == null ? null : new MapEntry<>(firstFound.position, elementTree);
    }

    protected static TreeElement getFirstMatchedElementInTree(List<TreeElement> elementTree, Element searchElement)
    {
        if(hasItem(elementTree))
        {
            for (TreeElement item : elementTree)
            {
                if (elementEquals(searchElement, item.element))
                {
                    return item;
                }
            }
        }

        return null;
    }

    protected static List<SearchElementRecord> getFoundElementRecordsWithShortestDistanceToAnchorElement(List<SearchElementRecord> foundElementRecords)
    {
        int shortestDistance = -1;
        List<SearchElementRecord> searchElementRecordsWithShortestDistance = new ArrayList<>();

        if (hasItem(foundElementRecords))
        {
            for (SearchElementRecord item : foundElementRecords)
            {
                int distance = item.distanceToAnchorElement;

                if (shortestDistance == -1 || distance < shortestDistance)
                {
                    shortestDistance = distance;
                    searchElementRecordsWithShortestDistance = new ArrayList<>();
                    searchElementRecordsWithShortestDistance.add(item);
                }
                else if (distance == shortestDistance)
                {
                    searchElementRecordsWithShortestDistance.add(item);
                }
            }
        }

        return  searchElementRecordsWithShortestDistance;
    }

    protected static List<Element> getElementsByTagNameMatchingOwnText(Document document,
                                                                       String tagName,
                                                                       String pattern,
                                                                       boolean caseSensitive,
                                                                       boolean exactMatch,
                                                                       boolean ignoreTabsAndSpaces)
    {
        List<Element> elements = getElementsByTagName(document, tagName);
        return getElementsMatchingOwnText(elements, pattern, caseSensitive, exactMatch, ignoreTabsAndSpaces);
    }

    protected static List<Element> getElementsMatchingOwnText(Document document,
                                                              String pattern,
                                                              boolean caseSensitive,
                                                              boolean exactMatch,
                                                              boolean ignoreTabsAndSpaces)
    {
        List<Element> elements = document == null ? new ArrayList<>() : document.getAllElements();
        return getElementsMatchingOwnText(elements, pattern, caseSensitive, exactMatch, ignoreTabsAndSpaces);
    }

    protected static List<Element> getElementsMatchingOwnText(List<Element> elements,
                                                              String pattern,
                                                              boolean caseSensitive,
                                                              boolean exactMatch,
                                                              boolean ignoreTabsAndSpaces)
    {
        List<Element> result = new ArrayList<>();

        if(hasItem(elements))
        {
            for (Element item : elements)
            {
                if(matchElementOwnText(item, pattern, caseSensitive, exactMatch, ignoreTabsAndSpaces))
                {
                    result.add(item);
                }
            }
        }

        return result;
    }

    protected static boolean matchElementOwnText(Element element,
                                                 String pattern,
                                                 boolean caseSensitive,
                                                 boolean exactMatch,
                                                 boolean ignoreTabsAndSpaces)
    {
        if(element == null || element.ownText() == null || pattern == null)
        {
            return false;
        }

        String elementOwnText = ignoreTabsAndSpaces ? element.ownText().replace("\\s+", "") : element.ownText();
        String patternWithoutSpaces = ignoreTabsAndSpaces ? pattern.replace("\\s+", "") : pattern;

        if(caseSensitive && exactMatch)
        {
            if (elementOwnText.equals(patternWithoutSpaces))
            {
                return true;
            }
        }
        else if (caseSensitive)
        {
            if(elementOwnText.contains(patternWithoutSpaces))
            {
                return true;
            }
        }
        else if(exactMatch)
        {
            if(elementOwnText.equalsIgnoreCase(patternWithoutSpaces))
            {
                return true;
            }
        }
        else
        {
            if(elementOwnText.toLowerCase().contains(patternWithoutSpaces.toLowerCase()))
            {
                return true;
            }
        }

        return false;
    }

    protected static List<Element> getElementsByTagName(Document document, String tagName)
    {
        List<Element> result = new ArrayList<>();

        if(document != null)
        {
            List<TreeElement> elementTree = getHtmlDocumentElementTree(document);

            for (TreeElement item : elementTree)
            {
                String elementTagName = item.element.tagName();

                if(elementTagName.trim().equalsIgnoreCase(tagName.trim()))
                {
                    result.add(item.element);
                }
            }
        }

        return result;
    }

    protected static String buildXpathPartBetweenRootAndLeafExcludingRoot(Element root, Element leaf)
    {
        List<Element> allElements = getElementsBetweenLeafAndRootInclusive(leaf, root);

        if(hasNoItem(allElements))
        {
            return null;
        }

        int elementCount = allElements.size();

        if(elementCount == 1)
        {
            return "";
        }

        StringBuilder xpathBuilder = new StringBuilder();

        for(int i = elementCount - 2; i >= 0; i--)
        {
            String tagName = allElements.get(i).tagName();
            xpathBuilder.append(tagName);

            if(i > 0)
            {
                xpathBuilder.append("/");
            }
        }

        return xpathBuilder.toString();
    }

    protected static List<Element> getElementsBetweenLeafAndRootInclusive(Element leaf, Element root)
    {
        List<Element> result = new ArrayList<>();
        result.add(leaf);
        Element currentParent = leaf;

        do
        {
            if(elementEquals(currentParent, root))
            {
                break;
            }
            else
            {
                currentParent = currentParent.parent();
                result.add(currentParent);
            }
        }
        while(true);

        return result;
    }

    /**
     * same outcome as jsoup.getAllElements()
     */
    protected static List<TreeElement> getHtmlDocumentElementTree(Document document)
    {
        Element htmlElement = getHtmlElement(document);
        return htmlElement == null ? new ArrayList<>() : getElementTree(htmlElement);
    }

    protected static TreeElement getRootElement(List<TreeElement> tree)
    {
        List<Integer> rootPosition = new ArrayList<>();
        rootPosition.add(0);

        for (TreeElement item : tree)
        {
            if(item.position.equals(rootPosition))
            {
                return item;
            }
        }

        return null;
    }

    protected static List<TreeElement> getElementTree(Element element)
    {
        List<TreeElement> tree = new ArrayList<>();
        TreeElement rootElement = new TreeElement();
        rootElement.position.add(0);
        rootElement.element = element;
        tree.add(rootElement);
        List<TreeElement> allChildren = getAllChildren(element, rootElement.position);
        tree.addAll(allChildren);

        return tree;
    }

    protected static Element getHtmlElement(Document document)
    {
        Elements elements = document == null ? null : document.select("html");
        return hasItem(elements) ? elements.first() : null;
    }

    protected static Elements toElements(List<Element> elements)
    {
        Elements result = new Elements();

        if(hasItem(elements))
        {
            for(Element item : elements)
            {
                result.add(item);
            }
        }

        return result;
    }

    protected static boolean elementEquals(Element element1, Element element2)
    {
        boolean result = element1 != null && element2 != null && element1.equals(element2);
        return result ? true : element1 != null && element2 != null &&
                element1.tagName().equals(element2.tagName()) &&
                element1.toString().equals(element2.toString());
    }

    protected static <T> boolean hasItem(List<T> list)
    {
        return (list != null && !list.isEmpty());
    }

    protected static <T> boolean hasNoItem(List<T> list)
    {
        return (list == null || list.isEmpty());
    }

    private static WebElement findWebElement(WebDriver driver, Document document, Elements anchorElements, String searchCssQuery)
    {
        if(document == null)
        {
            return null;
        }

        Elements searchElements = document.select(searchCssQuery);

        if(hasItem(anchorElements) && hasItem(searchElements))
        {
            for (Element item : anchorElements)
            {
                try
                {
                    String xpath = getXPath(item, searchElements);
                    return driver.findElement(By.xpath(xpath));
                }
                catch(AmbiguousFoundXPathsException e){}
            }
        }

        return null;
    }

    private static List<TreeElement> getAllChildren(Element element, List<Integer> startingPosition)
    {
        List<TreeElement> result = new ArrayList<>();

        if(element != null)
        {
            Elements children = element.children();

            if(hasItem(children))
            {
                for(int i = 0; i < children.size(); i++)
                {
                    TreeElement treeElement = new TreeElement();
                    treeElement.position = new ArrayList<>(startingPosition);
                    treeElement.position.add(i);
                    treeElement.element = children.get(i);
                    result.add(treeElement);
                    List<TreeElement> nextResult = getAllChildren(treeElement.element, treeElement.position);
                    result.addAll(nextResult);
                }
            }
        }

        return result;
    }
}