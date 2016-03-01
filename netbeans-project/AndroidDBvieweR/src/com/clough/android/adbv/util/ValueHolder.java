/*
 * Copyright (C) 2016 Thedath Oudarya
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.clough.android.adbv.util;

import com.clough.android.adbv.Launcher;
import javax.swing.ImageIcon;

/**
 *
 * @author Thedath Oudarya
 */
public class ValueHolder {
    
    public static final String APP_VERSION = "1.0.1";
    
    public static final String WINDOW_TITLE = "AndroidDBvieweR " + APP_VERSION;

    public interface Icons {
        
        public static final ImageIcon APPLICATION = new ImageIcon(Launcher.class.getResource("/icons/application_icon.png"));
        
        public static final ImageIcon DATABASE = new ImageIcon(Launcher.class.getResource("/icons/database.png"));
        
        public static final ImageIcon TABLE = new ImageIcon(Launcher.class.getResource("/icons/sql_table.png"));
        
        public static final ImageIcon PENCIL = new ImageIcon(Launcher.class.getResource("/icons/pencil.png"));
        
        public static final ImageIcon HASH_TAG = new ImageIcon(Launcher.class.getResource("/icons/hash_tag.png"));
        
        public static final ImageIcon BLUE = new ImageIcon(Launcher.class.getResource("/icons/blue.png"));
        
        public static final ImageIcon GREEN = new ImageIcon(Launcher.class.getResource("/icons/green.png"));
        
        public static final ImageIcon ORANGE = new ImageIcon(Launcher.class.getResource("/icons/orange.png"));
    
    }

}
