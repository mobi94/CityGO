//
//  CGViewControllerProtected.h
//  CityGo
//
//  Created by ruslan on 1/22/15.
//  Copyright (c) 2015 Ruslan Moskalenko. All rights reserved.
//

#import "MBProgressHUD.h"

@interface CGViewController ()

@property(strong, nonatomic) id notificationsObserver;

- (void)registerObserver;
- (void)unregisterObserver;

- (void)handleError:(NSError *)error;

- (void)showHUD;
- (void)hideHUD;

@end
