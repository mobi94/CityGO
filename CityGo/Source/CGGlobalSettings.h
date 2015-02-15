//
//  CGGlobalSettings.h
//  CityGo
//
//  Created by ruslan on 1/22/15.
//  Copyright (c) 2015 Ruslan Moskalenko. All rights reserved.
//

#pragma mark -
#pragma mark Fonts

#define CG_DEFAULT_FONT_NAME_REGULAR @"HelveticaNeue"
#define CG_DEFAULT_FONT_NAME_BOLD @"HelveticaNeue-Bold"
#define CG_DEFAULT_FONT_NAME_MEDIUM @"HelveticaNeue-Medium"
#define CG_DEFAULT_FONT_NAME_MEDIUM_ITALIC @"HelveticaNeue-Italic"
#define CG_DEFAULT_FONT_NAME_THIN @"HelveticaNeue-Thin"

#pragma mark -
#pragma mark Font sizes

#define CG_FONT_SIZE_NORMAL 16.0
#define CG_FONT_SIZE_SMALL 14.0
#define CG_FONT_SIZE_FOR_MENU 32.0
#define CG_FONT_SIZE_FOR_PROFILE 21.0

#pragma mark -
#pragma mark Colors

#define RGB(r, g, b) [UIColor colorWithRed:r / 255.0 green:g / 255.0 blue:b / 255.0 alpha:255.0]

#define CG_WHITE_COLOR [UIColor whiteColor]
#define CG_BLACK_COLOR [UIColor blackColor]
#define CG_GRAY_COLOR [UIColor grayColor]
#define CG_RED_COLOR [UIColor redColor]
#define CG_GREEN_COLOR [UIColor greenColor]
#define CG_BLUE_COLOR [UIColor blueColor]

#pragma mark -
#pragma mark Foundation singletons

#define NOTIFICATION_CENTER [NSNotificationCenter defaultCenter]
#define STANDART_USER_DEFAULTS [NSUserDefaults standardUserDefaults]
#define MAIN_QUEUE [NSOperationQueue mainQueue]