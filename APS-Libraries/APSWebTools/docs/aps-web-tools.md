# APSWebTools

This is not an OSGi bundle! This is a plain jar containing utilities for web applications. Specifically APS administration web applications. This jar has to be included in each web application that wants to use it.

Among other things it provides support for being part of the APS administration web login (APSAdminWebLoginHandler). Since the APS administration web is built using Vaadin it has Vaadin support classes. APSVaadinOSGiApplication is a base class used by all APS administration webs.

## APIs

public _class_ __APSAdminWebLoginHandler__ extends  APSLoginHandler  implements  APSLoginHandler.HandlerInfo    [se.natusoft.osgi.aps.tools.web] {

>  This is a login handler to use by any admin web registering with the APSAdminWeb to validate that there is a valid login available. 



__public APSAdminWebLoginHandler(BundleContext context)__

>  Creates a new APSAdminWebLoginHandler.  

_Parameters_

> _context_ - The bundle context. 

__public void setSessionIdFromRequestCookie(HttpServletRequest request)__

>  Sets the session id from a cookie in the specified request.  

_Parameters_

> _request_ - The request to get the session id cookie from. 

__public void saveSessionIdOnResponse(HttpServletResponse response)__

>  Saves the current session id on the specified response.  

_Parameters_

> _response_ - The response to save the session id cookie on. 

__public String getSessionId()__

>  

_Returns_

> An id to an APSSessionService session.

__public void setSessionId(String sessionId)__

>  Sets a new session id.  

_Parameters_

> _sessionId_ - The session id to set. 

__public String getUserSessionName()__

>  

_Returns_

> The name of the session data containing the logged in user if any.

__public String getRequiredRole()__

>  

_Returns_

> The required role of the user for it to be considered logged in.

}

----

    

public _class_ __APSLoginHandler__ implements  LoginHandler    [se.natusoft.osgi.aps.tools.web] {

>  This class validates if there is a valid logged in user and also provides a simple login if no valid logged in user exists. 

> This utility makes use of APSSimpleUserService to login auth and APSSessionService for session handling. Trackers for these services are created internally which requires the shutdown() method to be called when no longer used to cleanup. 

> The bundle needs to import the following packages for this class to work: 

        <code>
           se.natusoft.osgi.aps.api.auth.user;version="[0.9,2)",
           se.natusoft.osgi.aps.api.auth.user.model;version="[0.9,2)",
           se.natusoft.osgi.aps.api.misc.session;version="[0.9,2)"
        </code>

> 















__public APSLoginHandler(BundleContext context, HandlerInfo handlerInfo)__

>  Creates a new VaadinLoginDialogHandler.  

_Parameters_

> _context_ - The bundles BundleContext. 

__protected void setHandlerInfo(HandlerInfo handlerInfo)__

>  Sets the handler info when not provided in constructor.  

_Parameters_

> _handlerInfo_ - The handler info to set. 

__public void shutdown()__

>  Since this class internally creates and starts service trackers this method needs to be called on shutdown to cleanup! 

__public User getLoggedInUser()__

>  This returns the currently logged in user or null if none are logged in. 



__public boolean hasValidLogin()__

>  Returns true if this handler sits on a valid login. 

__public boolean login(String userId, String pw)__

>  Logs in with a userid and a password.  

_Returns_

> true if successfully logged in, false otherwise.

_Parameters_

> _userId_ - The id of the user to login. 

> _pw_ - The password of the user to login. 

__public User login(String userId, String pw, String requiredRole)__

>  Logs in with a userid and a password. 

> This method does not use or modify any internal state of this object! It only uses the APSUserService that this object sits on. This allows code sitting on an instance of this class to use this method for validating a user without having to setup its own service tracker for the APSUserService when this object is already available due to the code also being an APSAdminWeb member. It is basically a convenience.  

_Returns_

> a valid User object on success or null on failure.

_Parameters_

> _userId_ - The id of the user to login. 

> _pw_ - The password of the user to login. 

> _requiredRole_ - If non null the user is required to have this role for a successful login. If it doesn't null will 

public _static_ _interface_ __HandlerInfo__   [se.natusoft.osgi.aps.tools.web] {

>  Config values for the login handler. 

__String getSessionId()__

>  

_Returns_

> An id to an APSSessionService session.

__void setSessionId(String sessionId)__

>  Sets a new session id.  

_Parameters_

> _sessionId_ - The session id to set. 

__String getUserSessionName()__

>  

_Returns_

> The name of the session data containing the logged in user if any.

__String getRequiredRole()__

>  

_Returns_

> The required role of the user for it to be considered logged in.

}

----

    

public _class_ __ClientContext__   [se.natusoft.osgi.aps.tools.web] {

>  A context to pass to client code for use in calling services. 







__public ClientContext(UserMessager userMessager, OSGiBundleContextProvider bundleContextProvider)__

>  Creates a new ClientContext instance.  

_Parameters_

> _userMessager_ - Used to send messages to the user. 

> _bundleContextProvider_ - Provides the OSGi BundleContext. 

__public BundleContext getBundleContext()__

>  Returns the OSGi BundeContext. 

__public UserMessager getMessager()__

>  Use for producing messages to the user. 

__public <Service> void addService(Class<Service> serviceInterface, Service serviceImpl)__

>  Adds a service to the context.  

_Parameters_

> _serviceInterface_ - The interface of the service to add. 

> _serviceImpl_ - The implementation to the service. 

__public <Service> Service getService(Class<Service> serviceClass)__

>  Returns the service of the specified service interface.  

_Returns_

> The service.

_Parameters_

> _<Service>_ - The service type. 

> _serviceClass_ - The service type class. 

}

----

    

public _class_ __CookieTool__   [se.natusoft.osgi.aps.tools.web] {

>  Provides simple static cookie tools. 

__public static String getCookie(Cookie[] cookies, String name)__

>  Returns the cookie having the specified name or null if none is found.  

_Parameters_

> _cookies_ - The complete set of cookies from the request. 

__public static void setCookie( HttpServletResponse resp, String name, String value, int maxAge, String path)__

>  Sets a cookie on the specified response.  

_Parameters_

> _resp_ - The servlet response to set the cookie on. 

> _name_ - The name of the cookie. 

> _value_ - The value of the cookie. 

> _maxAge_ - The max age of the cookie. 

__public void deleteCookie(String name, HttpServletResponse resp)__

>  Removes a cookie.  

_Parameters_

> _name_ - The name of the cookie to remove. 

> _resp_ - The servlet response to remove the cookie on. 

}

----

    

public _interface_ __LoginHandler__   [se.natusoft.osgi.aps.tools.web] {

>  This is a simple API for doing a login. 

__public boolean hasValidLogin()__

>  Returns true if this handler sits on a valid login. 

__boolean login(String userId, String pw)__

>  Logs in with a userid and a password.  

_Returns_

> true if successfully logged in, false otherwise.

_Parameters_

> _userId_ - The id of the user to login. 

> _pw_ - The password of the user to login. 

__public void shutdown()__

>  If the handler creates service trackers or other things that needs to be shutdown when no longer used this methods needs to be called when the handles is no longer needed. 

}

----

    

public _interface_ __OSGiBundleContextProvider__   [se.natusoft.osgi.aps.tools.web] {

>  Gives access to the OSGi BundleContext. 

__public BundleContext getBundleContext()__

>  Returns the context for this deployed bundle. 

}

----

    

public _interface_ __UserMessager__   [se.natusoft.osgi.aps.tools.web] {

>  Handles presenting user with messages. 

> Different GUI choices needs different implementations of this. The basic idea behind this is to make message handling less dependent on GUI. Unit tests can also supply own implementation of this. 

__public void error(String caption, String message)__

>  Shows an error message on the window.  

_Parameters_

> _caption_ - The message caption. 

> _message_ - The message. 

__public void warning(String caption, String message)__

>  Shows a warning message on the window.  

_Parameters_

> _caption_ - The message caption. 

> _message_ - The message. 

__public void info(String caption, String message)__

>  Shows an info message on the window.  

_Parameters_

> _caption_ - The message caption. 

> _message_ - The message. 

__public void tray(String caption, String message)__

>  Shows a tray message on the window.  

_Parameters_

> _caption_ - The message caption. 

> _message_ - The message. 

}

----

    

public _class_ __APSSessionListener__ implements  HttpSessionListener  [se.natusoft.osgi.aps.tools.web.vaadin] {

>  This is registered in web.xml and installs itself in the session on session create using its class name as key. Web applications can then get this from the session and register themselves as "destroyed" listeners on this to be called when sessionDestroyed() is called on this. 

> The main reason for this is to be able to shutdown and release any service trackers that were started on session startup. 



__public void addDestroyedListener(APSSessionDestroyedListener destroyedListener)__

>  Adds a session destroyed listener to forward session destroyed events to.  

_Parameters_

> _destroyedListener_

__public void sessionCreated(HttpSessionEvent httpSessionEvent)__

>  Gets called when a session is created.  

_Parameters_

> _httpSessionEvent_

__public void sessionDestroyed(HttpSessionEvent httpSessionEvent)__

>  Gets called when a session is destroyed.  

_Parameters_

> _httpSessionEvent_

public _static_ _interface_ __APSSessionDestroyedListener__   [se.natusoft.osgi.aps.tools.web.vaadin] {

>  This should be implemented by class wanting to receive the sessionDestroyed event. 

__public void sessionDestroyed()__

>  Gets called when the session is destroyed. 

}

----

    

public _class_ __APSTheme__   [se.natusoft.osgi.aps.tools.web.vaadin] {

>  The only purpose of this class is to define the vaadin theme used. 

}

----

    











__public BundleContext getBundleContext()__

>  This will return this war bundles BundleContext. This is only available if this war is deployed in an R4.2+ compliant OSGi container.  

_Returns_

> The OSGi bundle context.

__public void init()__

>  Initializes the vaadin application. 

__public void setMainWindow(Window mainWindow)__

>  Initializes the gui part of the applicaiton.  Initializes the service setup.  

_Parameters_

> _clientContext_ - The client context for accessing services. 

> _clientContext_ - The client cntext for accessing services. 

> _mainWindow_

__public ClientContext getClientContext()__

>  

_Returns_

> The client context.

__public void sessionDestroyed()__

>  Called when session is destroyed. 

}

----

    

public _class_ __HorizontalLine__ extends  Label    [se.natusoft.osgi.aps.tools.web.vaadin.components] {

>  This is a component that draws a horizontal line. 

__public HorizontalLine()__

>  Creates a new HorizontalLine. 

}

----

    

public _class_ __HTMLFileLabel__ extends  Label    [se.natusoft.osgi.aps.tools.web.vaadin.components] {

>  This is a Label that takes a classpath relative path to an html file and loads it as label content. Please note that it required XHTML! 

> Any comment blocks in the html are skipped when loading. 

__public HTMLFileLabel(String htmlFilePath, String themeName, ClassLoader classLoader)__

>  Creates a new HTMLFileLabel.  

_Parameters_

> _htmlFilePath_ - The label content html file. 

> _themeName_ - The name of the theme used. Any {vaadin-theme} in loaded html will be 

> _classLoader_ - The class loader to use for finding the html file path. 



}

----

    

public _interface_ __MenuBuilder<MenuItemRepresentative>__   [se.natusoft.osgi.aps.tools.web.vaadin.components.menutree.builderapi] {

>  This must be implemented by a provider of menu entries. 

__public void buildMenuEntries(HierarchicalModel<MenuItemData<MenuItemRepresentative>> menuModel)__

>  This should add menu entries to the received menu model.  

_Parameters_

> _menuModel_ - The model to add menu entries to. 

}

----

    

public _class_ __MenuItemData<ItemRepresentative>__   [se.natusoft.osgi.aps.tools.web.vaadin.components.menutree.builderapi] {

>  Holds item related information that is associated with a model item by its item id. 











__public ItemRepresentative getItemRepresentative()__

>  Creates a new MenuTreeItemData instance.  This represents one specific configuration. 

__public String getToolTipText()__

>  The tooltip text for the item. 

__public Action[] getActions()__

>  The actions for the item. 

__public ComponentHandler getSelectComponentHandler()__

>  The component hander for when this item is selected. 

__public Map<Action, MenuActionProvider> getActionComponentHandlers()__

>  The menu action handlers per action. 

}

----

    

public _interface_ __ComponentHandler__ extends  MenuActionProvider    [se.natusoft.osgi.aps.tools.web.vaadin.components.menutree.handlerapi] {

>  Implementations of this combines possibly a singleton instance of a GUI component with item specific data and then returns the component. 

__public AbstractComponent getComponent()__

>  

_Returns_

> The component that should handle the item.

}

----

    

public _interface_ __MenuActionExecutor__ extends  MenuActionProvider    [se.natusoft.osgi.aps.tools.web.vaadin.components.menutree.handlerapi] {

>  This is an alternative to ComponentHandler for when there is not need to display a component, just perform some action. 

__public void executeMenuAction()__

>  Executes the menu action. 

}

----

    

}

----

    

public _class_ __MenuTree__ extends  Tree  implements  Action.Handler,  ItemDescriptionGenerator,  Refreshable    [se.natusoft.osgi.aps.tools.web.vaadin.components.menutree] {

>  This is a semi advanced menu tree component. 

> You add 'MenuBuilder's to provide the menu contents. The builders also provide actions for when a menu entry is accessed. 

__public static final Action[] NO_ACTIONS = new Action[0]__

>  Indicates no actions. 







__public MenuTree()__

>  Creates a new MenuTree component. 

__public void addMenuBuilder(MenuBuilder menuBuilder)__

>  Adds a MenuBuilder to the menu tree.  

_Parameters_

> _menuBuilder_ - The menu builder to add. 

__public String generateDescription(Component source, Object itemId, Object propertyId)__

>  Called by Table when a cell (and row) is painted or a item is painted in Tree.  

_Returns_

> The description or "tooltip" of the item.

_Parameters_

> _source_ - The source of the generator, the Tree or Table the 

> _itemId_ - The itemId of the painted cell 

> _propertyId_ - The propertyId of the cell, null when getting row 

__public void refresh()__

>  Reloads the contents of the menu. This is never done automatically!! 

__public MenuItemData getItemData(IntID itemId)__

>  Returns a data object associated with a menu item by its item id.  

_Returns_

> The associated data or null.

_Parameters_

> _itemId_ - The item id of the item whose associated data to get. 

__public void setActionHandler(MenuActionHandler actionHandler)__

>  Sets the action handler for forward actions to.  

_Parameters_

> _actionHandler_ - The action handler to set. 



__public void expandHierarchicalModel()__

>  Expands all nodes in the hierarchical model. 

__public Action[] getActions(Object target, Object sender)__

>  Gets the list of actions applicable to this handler.  

_Returns_

> the list of Action

_Parameters_

> _target_ - the target handler to list actions for. For item 

> _sender_ - the party that would be sending the actions. Most of this 

__public void handleAction(Action action, Object sender, Object target)__

>  Handles an action for the given target. The handler method may just discard the action if it's not suitable.  

_Parameters_

> _action_ - the action to be handled. 

> _sender_ - the sender of the action. This is most often the action 

> _target_ - the target of the action. For item containers this is the 

public _static_ _interface_ __MenuActionHandler__   [se.natusoft.osgi.aps.tools.web.vaadin.components.menutree] {

>  This provides half of the Action.Handler API since we provide the first part our self. 

__public void handleAction(Action action, Object sender, Object target)__

>  Handles an action for the given target. The handler method may just discard the action if it's not suitable.  

_Parameters_

> _action_ - the action to be handled. 

> _sender_ - the sender of the action. This is most often the action 

> _target_ - the target of the action. For item containers this is the 

}

----

    

public _class_ __SidesAndCenterLayout__ extends  HorizontalLayout    [se.natusoft.osgi.aps.tools.web.vaadin.components] {

>  This is a layout that only accepts 5 components at 5 positions: left, top, center, bottom, right. 

> The layout looks like this: 

        +------+----------------+------+
        |      |                |      |
        |      +----------------+      |
        |      |                |      |
        |      |                |      |
        |      |                |      |
        |      +----------------+      |
        |      |                |      |
        +------+----------------+------+

> You set the different components with "setLeft(...), setTop(...), ...". All positions are optional, and you don't have to set any, but that would be pointless. 

> When you have set all your components the doLayout() method must be called, and it cannot be called before that. If you fail to call doLayout() there will be an exception when rendering. 













__public SidesAndCenterLayout()__

>  Creates a new SidesAndCenterLayout. 

__public void setLeft(Component left)__

>  Sets the component for the left side.  

_Parameters_

> _left_ - The component to set. 

__public Component getLeft()__

>  

_Returns_

> The left component or null if none.

__public void setTop(Component top)__

>  Sets the component for the top.  

_Parameters_

> _top_ - The component to set. 

__public Component getTop()__

>  

_Returns_

> The top component or null if none.

__public void setCenter(Component center)__

>  Sets the component for the center.  

_Parameters_

> _center_ - The component to set. 

__public Component getCenter()__

>  

_Returns_

> The center component or null if none.

__public void setBottom(Component bottom)__

>  Sets the component for the bottom.  

_Parameters_

> _bottom_ - The component to set. 

__public Component getBottom()__

>  

_Returns_

> The bottom component or null if none.

__public void setRight(Component right)__

>  Sets the component for the right side.  

_Parameters_

> _right_ - The component to set. 

__public Component getRight()__

>  

_Returns_

> The right component or null if none.

__public void doLayout()__

>  Does the actual layout adding the provided component to the layout. This must be called after the components have been set. 

> It starts by removing all components so if this has already been setup and this method called, it can be called again with new components added or components replaced with other components. 

__public void paintContent(PaintTarget target) throws PaintException__

>  This is only here to validate that doLayout() have been called before painting is done.  

_Parameters_

> _target_

_Throws_

> _PaintException_

}

----

    

public _class_ __HierarchicalModel<Data>__   [se.natusoft.osgi.aps.tools.web.vaadin.models] {

>  This is a wrapping of Vaadins HierarchicalContainer. 

> The reason for this is that I want to be able to associate a set of specific data with each item. For a Tree you can use item properties, but that is more of a side-effect than intentional usage. It will work less well with a Table. So this wrapper will build a HierarchicalContainer, generate item ids, which will also be used to associate a data object with each item in a separate map. Once the HierarchicalModel is built the HierarchicalContainer can be gotten from it and the data object for any item can be looked up by its id. 

> This does currently not support default values!  









__public HierarchicalModel(ID idProvider, String... captionProperties)__

>  Creates a new HierarchicalModel.  

_Parameters_

> _idProvider_ - An ID implementation providing IDs. 

> _captionProperties_ - container captions. 

__public HierarchicalModel(ID idProvider)__

>  Creates a new HierarchicalModel.  

_Parameters_

> _idProvider_ - An ID implementation providing IDs. 

__public static String getDefaultCaption()__

>  If the no arg constructor is used the caption property is made up, and this will return it.  

_Returns_

> The made up caption property.

__public ID addItem(ID parent, Data data, String... captions)__

>  Adds an item to the model.  

_Returns_

> The item id of the added item.

_Parameters_

> _parent_ - If non null this should be the item id of the parent of the item added. 

> _data_ - The data to associate with the item. 

> _captions_ - Caption values for the caption properties. 

__public ID addItem(Data data, String... captions)__

>  Adds an item to the model.  

_Returns_

> The item id of the added item.

_Parameters_

> _data_ - The data to associate with the item. 

> _captions_ - Caption values for the caption properties. 

__public Data getData(ID itemId)__

>  Returns the associated data for an item.  

_Returns_

> The associated data.

_Parameters_

> _itemId_ - The id of the item to get associated data for. 

__public ID getCurrentItemId()__

>  

_Returns_

> The current item id, which is also always the last/highest item id.

__public HierarchicalContainer getHierarchicalContainer()__

>  Returns the HierarchicalContainer that we have built so far. 

> Please note that it is the internal instance that is returned, not a copy of it! Thereby any changes made after the call to this method will still affect the returned object! 

}

----

    

public _interface_ __Refreshable__   [se.natusoft.osgi.aps.tools.web.vaadin.tools] {

>  This is a way to provide components that needs to be refreshed to other components without creating dependencies to the whole component. 

__public void refresh()__

>  Refreshes its content. 

}

----

    

public _class_ __Refreshables__ implements  Iterable<Refreshable>    [se.natusoft.osgi.aps.tools.web.vaadin.tools] {

>  Manages a set of Refreshable objects. 



__public void addRefreshable(Refreshable refreshable)__

>  Creates a new Refreshables instance.  Adds a refreshable to the refreshables.  

_Parameters_

> _refreshable_ - The refreshable to add. 

__public Iterator<Refreshable> iterator()__

>  Returns an iterator over a set of elements of type T.  

_Returns_

> an Iterator.

__public void refresh()__

>  Calls on the managed refreshables to do refresh. 

}

----

    

public _interface_ __RefreshableSupport__   [se.natusoft.osgi.aps.tools.web.vaadin.tools] {

>  This can be implemented by classes that supports adding Refreshables. 

__public void addRefreshable(Refreshable refreshable)__

>  Adds a refreshable to be passed to editors on menu entry selection.  

_Parameters_

> _refreshable_ - The refreshable to add. 

}

----

    

public _class_ __VaadinLoginDialogHandler__   [se.natusoft.osgi.aps.tools.web.vaadin] {

>  This is a Vaadin based login dialog. 













__public VaadinLoginDialogHandler(Window appWindow, LoginHandler loginHandler)__

>  Creates a new VaadinLoginDialogHandler.  

_Parameters_

> _appWindow_ - The Vaadin application window to add the popup login dialog to. 

> _loginHandler_ - A handler for doing the login from the login dialog input. 

__public void setLoginDialogTitle(String loginDialogTitle)__

>  Sets the title of the login dialog window.  

_Parameters_

> _loginDialogTitle_ - The title to set. 

__public void doLoginDialog()__

>  This will popup the login dialog if it is not already showing. Any previously entered user and password are ofcourse cleared! 



}

----

    

public _class_ __VaadinUserMessager__ implements  UserMessager    [se.natusoft.osgi.aps.tools.web.vaadin] {

>  Implementation of UserMessager for Vaadin applications using a Vaadin Window to do showNotification(...) on. 



__public VaadinUserMessager()__

>  Creates a new VaadinUserMessager instance. 

__public void setMessageWindow(Window messageWindow)__

>  Sets the message window.  

_Parameters_

> _messageWindow_ - The message window to set. 

__public void error(String caption, String message)__

>  Shows an error message on the window.  

_Parameters_

> _caption_ - The message caption. 

> _message_ - The message. 

__public void warning(String caption, String message)__

>  Shows a warning message on the window.  

_Parameters_

> _caption_ - The message caption. 

> _message_ - The message. 

__public void info(String caption, String message)__

>  Shows an info message on the window.  

_Parameters_

> _caption_ - The message caption. 

> _message_ - The message. 

__public void tray(String caption, String message)__

>  Shows a tray message on the window.  

_Parameters_

> _caption_ - The message caption. 

> _message_ - The message. 

}

----

    

