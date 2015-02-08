//
//  CGSignUpGateway.m
//  CityGo
//
//  Created by ruslan on 1/27/15.
//  Copyright (c) 2015 Ruslan Moskalenko. All rights reserved.
//

#import "CGSignUpGateway.h"

@implementation CGSignUpGateway

- (void)signInUp:(NSDictionary *)userInfo WithSuccess:(SignUpSuccessBlock)successBlock failure:(SignUpFailureBlock)failureBloc
{
    PFUser *user = [PFUser user];
    user.username = userInfo[@"username"];
    user.password = userInfo[@"password"];
    user.email = userInfo[@"email"];
    
    user[@"nickname"] = userInfo[@"username"];
    user[@"gender"] = userInfo[@"gender"];
    user[@"birthday"] = userInfo[@"birthday"];
    
    [user signUpInBackgroundWithBlock:^(BOOL succeeded, NSError *error)
     {
         if (!error)
         {
             // Hooray! Let them use the app now.
             NSData *imageData = [NSData data];
             
             if ([[userInfo allKeys] containsObject:@"photo"])
             {
                 UIImage *image = [userInfo valueForKey:@"photo"];
    
                 imageData = UIImagePNGRepresentation(image);
                 PFFile *imageFile = [PFFile fileWithName:[NSString stringWithFormat:@"%@savatar", user.username] data:imageData];
                 
                 [imageFile saveInBackgroundWithBlock:^(BOOL succeeded, NSError *error)
                  {
                      PFUser *user = [PFUser currentUser];
                      [user setObject:imageFile forKey:@"profilePic"];
                      [user saveInBackgroundWithBlock:^(BOOL succeeded, NSError *error)
                       {
                           if (error)
                           {
                               failureBloc(error);
                           }
                           else
                           {
                               successBlock(succeeded);
                           }
                       }];
                      
                  }];
             }
             else
             {
                 successBlock(succeeded);
             }
         }
         else
         {
             failureBloc(error);
         }
     }];
}

@end
