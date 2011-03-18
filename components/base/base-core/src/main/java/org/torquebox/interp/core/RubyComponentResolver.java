/*
 * Copyright 2008-2011 Red Hat, Inc, and individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.torquebox.interp.core;

import java.util.Collection;
import java.util.Map;

import org.jboss.logging.Logger;
import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;
import org.torquebox.injection.spi.InjectableRegistry;


public class RubyComponentResolver {

    public RubyComponentResolver() {
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public String getComponentName() {
        return this.componentName;
    }

    public boolean isAlwaysReload() {
        return alwaysReload;
    }

    public void setAlwaysReload(boolean alwaysReload) {
        this.alwaysReload = alwaysReload;
    }

    public void setRubyClassName(String rubyClassName) {
        this.rubyClassName = rubyClassName;
    }

    public String getRubyClassName() {
        return this.rubyClassName;
    }

    public void setRubyRequirePath(String rubyRequirePath) {
        this.rubyRequirePath = rubyRequirePath;
    }

    public String getRubyRequirePath() {
        return this.rubyRequirePath;
    }

    public void setInitializeParams(Object[] initializeParams) {
        this.initializeParams = initializeParams;
    }

    public void setInitializeParams(Collection params) {
        if (params != null)
            setInitializeParams( params.toArray() );
    }

    public void setInitializeParams(Map params) {
        if (params != null)
            setInitializeParams( new Object[] { params } );
    }

    public Object[] getInitializeParams() {
        return this.initializeParams;
    }

    public void setRegistry(InjectableRegistry registry) {
        this.registry = registry;
    }

    public InjectableRegistry getRegistry() {
        return this.registry;
    }

    public IRubyObject resolve(Ruby ruby) throws Exception {
        log.debug( "resolve(" + ruby + ")" );
        synchronized (ruby) {
            log.debug( "Got exclusive access: " + ruby );
            ruby.getLoadService().require(  "rubygems"  );
            ruby.getLoadService().require(  "torquebox-base"  );
            ruby.getLoadService().require(  "torquebox/component_manager"  );
            RubyClass managerClass = (RubyClass) ruby.getClassFromPath( "TorqueBox::ComponentManager" );
            log.debug( "Got manager: " + managerClass );
            IRubyObject component = (IRubyObject) JavaEmbedUtils.invokeMethod( ruby, managerClass, "lookup_component", new Object[] { this.componentName },
                    IRubyObject.class );
            log.debug( "Looked up component: " + component );

            if (isAlwaysReload() || component == null || component.isNil()) {
                component = createComponent( ruby );
                log.debug( "Created component: " + component );
                if (component != null) {
                    JavaEmbedUtils.invokeMethod( ruby, managerClass, "register_component", new Object[] { this.componentName, component }, void.class );
                    log.debug( "Registered component: " + component );
                }
                if (isAlwaysReload()) {
                    ruby.evalScriptlet( "Dispatcher.cleanup_application if defined?(Dispatcher)" );
                    log.debug( "Reloaded ruby: " + ruby );
                }
            }

            return component;
        }
    }

    protected IRubyObject createComponent(Ruby ruby) throws Exception {
        log.debug( "createComponent(" + ruby + ")" );
        if (this.rubyRequirePath != null) {
            ruby.getLoadService().load( this.rubyRequirePath + ".rb", false );
            log.debug( "Loaded source file: " + this.rubyRequirePath + ".rb" );
        }

        RubyClass componentClass = (RubyClass) ruby.getClassFromPath( this.rubyClassName );
        if (componentClass == null || componentClass.isNil()) {
            return null;
        }
        JavaEmbedUtils.invokeMethod( ruby, componentClass, "const_set", new Object[] { "TORQUEBOX_INJECTION_REGISTRY", getRegistry() }, Object.class );
        IRubyObject component = (IRubyObject) JavaEmbedUtils.invokeMethod( ruby, componentClass, "new", getInitializeParams(), IRubyObject.class );
        return component;
    }

    private String componentName;
    private boolean alwaysReload;
    private String rubyClassName;
    private String rubyRequirePath;
    private Object[] initializeParams;
    private InjectableRegistry registry;

    private static final Logger log = Logger.getLogger( RubyComponentResolver.class );

}
